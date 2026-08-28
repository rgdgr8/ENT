package de.esolutions.connectedradioclient.handlers.backend.jobs

import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.RequestHeader
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.data.extensions.buildUrl
import de.esolutions.connectedradioclient.data.extensions.replaceEsoUrlPlaceholder
import de.esolutions.connectedradioclient.handlers.backend.callbacks.LogoCallback
import de.esolutions.connectedradioclient.handlers.backend.exceptions.RegistrationFailureException
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
import java.net.URLEncoder

class LogoRequestJob(
    private val mediaRequestObject: MediaRequestObject,
    private val registrationHandler: HandleRegistration,
    private val tokenHandler: TokenHandler
) : IJob {

    private val bearer = mediaRequestObject.bearer.getBearerString()

    private val requestID = Http.generateRequestID()
    private val logInfo = LogInfo(bearer, requestID)

    override fun run() {
        Logger.d(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Start logo request job and checking if request should be sent",
            LoggerTags.LOGO
        )

        ServerDowntimeHandler.getRemainingDowntime()?.let {
            SendToMedia.retryAfter(
                logInfo,
                mediaRequestObject.callback,
                it,
                null
            )
            return
        }

        try {
            registrationHandler.ensureRegistration()
        } catch (ex: RegistrationFailureException) {
            if (ex.responseCode == null) {
                SendToMedia.error(logInfo, RetryBehavior.DELAYED, mediaRequestObject.callback)
                return
            } else {
                // Use exponential here as a fallback, as unforeseen problems such as parsing errors
                // or an invalid json should also lead to retries being executed
                val retryBehavior = Config.getBackendPreferredRetryBehavior(ex.responseCode, RetryBehavior.EXPONENTIAL)
                val contentExpiry = Config.getBackendPreferredContentExpiry(ex.responseCode)

                SendToMedia.retryAfterOrBackendPreferences(
                    logInfo,
                    mediaRequestObject.callback,
                    ex.retryAfter,
                    retryBehavior,
                    contentExpiry
                )
                return
            }
        } catch (e: Exception) {
            Logger.ex(
                PrivacyFlags.UNDECIDED,
                logInfo,
                "An error occurred during ensureRegistration.",
                e,
                LoggerTags.LOGO
            )
            SendToMedia.error(logInfo, RetryBehavior.EXPONENTIAL, mediaRequestObject.callback)
            return
        }

        val token = tokenHandler.getToken(Config.endpointConfig.operationId)
        if (token == null) {
            IRC.error(IRCEntry.LOGO, IRCMessage.TOKEN)
            SendToMedia.error(logInfo, RetryBehavior.DELAYED, mediaRequestObject.callback)
            return
        }

        val header = RequestHeader(token, requestID)
        header.mcc = mediaRequestObject.mcc
        header.ifNoneMatch = mediaRequestObject.eTags.logoETag
        header.accept = "image/*"

        try {
            val bearerUrl = URLEncoder.encode(bearer, "utf-8")
            val relativeUrl = Config.apiEndpoints.logo.replaceEsoUrlPlaceholder("bearer", bearerUrl)
            val url = Config.endpointConfig.url.buildUrl(relativeUrl)

            Logger.i(
                PrivacyFlags.UNDECIDED,
                logInfo,
                "Start logo request for $bearer",
                LoggerTags.LOGO,
                LoggerTags.REQUEST
            )

            Http.enqueueGet(url, header, LogoCallback(bearer, mediaRequestObject.callback))
        } catch (ex: IllegalArgumentException) {
            IRC.error(IRCEntry.EXCEPTIONS, "Logo=${ex.javaClass.simpleName}-${ex.message}")
            Logger.ex(
                PrivacyFlags.UNDECIDED,
                logInfo,
                "Logo failure",
                ex,
                LoggerTags.REQUEST,
                LoggerTags.LOGO
            )
            SendToMedia.error(logInfo, RetryBehavior.EXPONENTIAL, mediaRequestObject.callback)
        }
    }
}