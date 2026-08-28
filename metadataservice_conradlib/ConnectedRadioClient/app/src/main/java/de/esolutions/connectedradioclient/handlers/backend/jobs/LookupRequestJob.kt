package de.esolutions.connectedradioclient.handlers.backend.jobs

import de.esolutions.connectedradioclient.api.GenericMoshiParser
import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.RequestHeader
import de.esolutions.connectedradioclient.data.backend.lookup.request.ETagRequest
import de.esolutions.connectedradioclient.data.backend.lookup.request.StationInformation
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.data.extensions.buildUrl
import de.esolutions.connectedradioclient.handlers.backend.callbacks.LookupCallback
import de.esolutions.connectedradioclient.handlers.backend.exceptions.RegistrationFailureException
import de.esolutions.connectedradioclient.handlers.backend.executor.ExecutorWorker
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.handlers.media.SendToMedia
import de.esolutions.connectedradioclient.irc.IRC
import de.esolutions.connectedradioclient.irc.IRCEntry
import de.esolutions.connectedradioclient.irc.IRCMessage
import de.esolutions.connectedradioclient.logger.LogInfo
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags

class LookupRequestJob(
    private val container: Map<String, MediaRequestObject>,
    private val mcc: String?,
    private val registrationHandler: HandleRegistration,
    private val tokenHandler: TokenHandler
) : IJob {

    private val requestID = Http.generateRequestID()
    private val bearerStringList = container.keys.joinToString(", ")

    override fun run() {
        Logger.d(
            PrivacyFlags.UNDECIDED,
            LogInfo(bearerStringList, requestID),
            "Start lookup request job and checking if request should be sent",
            LoggerTags.LOOKUP
        )

        ServerDowntimeHandler.getRemainingDowntime()?.let {
            SendToMedia.retryAfterForAll(
                container,
                requestID,
                it,
                null
            )
            return
        }

        try {
            registrationHandler.ensureRegistration()
        } catch (ex: RegistrationFailureException) {
            if (ex.responseCode == null) {
                SendToMedia.errorForAll(RetryBehavior.DELAYED, container, requestID)
                return
            } else {
                // Use exponential here as a fallback, as unforeseen problems such as parsing errors
                // or an invalid json should also lead to retries being executed
                val retryBehavior = Config.getBackendPreferredRetryBehavior(ex.responseCode, RetryBehavior.EXPONENTIAL)
                val contentExpiry = Config.getBackendPreferredContentExpiry(ex.responseCode)

                SendToMedia.retryAfterOrBackendPreferencesForAll(
                    container,
                    requestID,
                    ex.retryAfter,
                    retryBehavior,
                    contentExpiry
                )
                return
            }
        } catch (e: Exception) {
            Logger.ex(
                PrivacyFlags.UNDECIDED,
                LogInfo(bearerStringList, requestID),
                "An error occurred during ensureRegistration.",
                e,
                LoggerTags.LOOKUP
            )
            SendToMedia.errorForAll(RetryBehavior.EXPONENTIAL, container, requestID)
            return
        }

        val token = tokenHandler.getToken(Config.endpointConfig.operationId)
        if (token == null) {
            IRC.error(IRCEntry.LOOKUP, IRCMessage.TOKEN)
            SendToMedia.errorForAll(RetryBehavior.DELAYED, container, requestID)
            return
        }

        val cleanContainer = container.mapValues {
            StationInformation(
                ETagRequest(it.value.eTags)
            )
        }
        Logger.d(
            PrivacyFlags.UNDECIDED,
            LogInfo(bearerStringList, requestID),
            "Start cleanContainer parsing with moshi",
            LoggerTags.LOOKUP
        )
        val requestJson = GenericMoshiParser.parseToJson(cleanContainer)
        val lookupCallback = LookupCallback(container, mcc) { mediaRequestObject ->
            val job = LogoRequestJob(mediaRequestObject, registrationHandler, tokenHandler)
            ExecutorWorker.enqueue(job)
        }

        val header = RequestHeader(token, requestID)
        header.mcc = mcc
        header.accept = "application/json"

        try {
            val url = Config.endpointConfig.url.buildUrl(Config.apiEndpoints.lookup)

            container.forEach { (bearer, mediaRequestObject) ->
                Logger.i(
                    PrivacyFlags.UNDECIDED,
                    LogInfo(bearer, requestID),
                    "Start request: $mediaRequestObject",
                    LoggerTags.LOOKUP,
                    LoggerTags.REQUEST
                )
            }

            Http.enqueuePost(url, header, requestJson, lookupCallback)
        } catch (ex: IllegalArgumentException) {
            IRC.error(IRCEntry.EXCEPTIONS, "Lookup=${ex.javaClass.simpleName}-${ex.message}")
            Logger.ex(
                PrivacyFlags.UNDECIDED,
                LogInfo(bearerStringList, requestID),
                "Lookup failure",
                ex,
                LoggerTags.REQUEST,
                LoggerTags.LOOKUP
            )
            SendToMedia.errorForAll(RetryBehavior.EXPONENTIAL, container, requestID)
        }
    }
}