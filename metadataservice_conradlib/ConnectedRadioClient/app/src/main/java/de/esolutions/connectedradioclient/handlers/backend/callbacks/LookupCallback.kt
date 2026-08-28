package de.esolutions.connectedradioclient.handlers.backend.callbacks

import com.squareup.moshi.Types
import de.esolutions.connectedradioclient.api.GenericMoshiParser
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.NO_CHANGES
import de.esolutions.connectedradioclient.data.backend.lookup.result.LookupResult
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.data.extensions.getRequestID
import de.esolutions.connectedradioclient.data.extensions.getRetryAfter
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.handlers.media.SendToMedia
import de.esolutions.connectedradioclient.irc.IRC
import de.esolutions.connectedradioclient.irc.IRCEntry
import de.esolutions.connectedradioclient.irc.IRCMessage
import de.esolutions.connectedradioclient.logger.LogInfo
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.io.IOException
import java.lang.reflect.ParameterizedType
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

class LookupCallback(
    private val lookupRequests: Map<String, MediaRequestObject>,
    private val mcc: String?,
    private val requestAvailableImage: (MediaRequestObject) -> Unit
) : Callback {

    private val bearerList = lookupRequests.keys.joinToString(", ")
    private var requestId: String? = null

    /**
     * Send error messages for all callbacks from media.
     */
    override fun onFailure(call: Call, e: IOException) {
        requestId = call.request().getRequestID()

        IRC.error(IRCEntry.EXCEPTIONS, "Lookup=${e.javaClass.simpleName}-${e.message}")
        Logger.ex(
            PrivacyFlags.UNDECIDED,
            LogInfo(bearerList, requestId),
            "Lookup failed",
            e,
            LoggerTags.RESPONSE,
            LoggerTags.LOOKUP
        )
        SendToMedia.errorForAll(RetryBehavior.DELAYED, lookupRequests, requestId)
    }

    /**
     * Try to parse the backend data to objects and return the information to media on success.
     */
    override fun onResponse(call: Call, response: Response) {
        requestId = response.getRequestID()

        val body = response.body?.string()
        logResponse(response, body)

        val retryAfter = response.getRetryAfter()
        val retryBehavior = Config.getBackendPreferredRetryBehavior(response.code)
        val contentExpiry = Config.getBackendPreferredContentExpiry(response.code)

        when (response.code) {
            200, 207 -> handleSuccessfulResponse(body)
            204 -> SendToMedia.noContentForAll(lookupRequests, requestId, contentExpiry)
            304 -> SendToMedia.notModifiedForAll(lookupRequests, requestId, contentExpiry)
            429, 503 -> {
                ServerDowntimeHandler.handleResponse(retryAfter)
                SendToMedia.retryAfterOrBackendPreferencesForAll(
                    lookupRequests,
                    requestId,
                    retryAfter,
                    retryBehavior,
                    contentExpiry
                )
            }

            else -> SendToMedia.retryAfterOrBackendPreferencesForAll(
                lookupRequests,
                requestId,
                retryAfter,
                retryBehavior,
                contentExpiry
            )
        }
    }

    /**
     * Handle response body if http code is 200.
     */
    private fun handleSuccessfulResponse(body: String?) {
        val lookupResults = body?.let {
            val parameterizedType: ParameterizedType = Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                LookupResult::class.java
            )
            GenericMoshiParser.parseFromJson<Map<String, LookupResult>>(
                it,
                parameterizedType
            )
        }
        lookupResults?.let {
            handleLookupResults(it)
            return
        }
        IRC.error(IRCEntry.LOOKUP, IRCMessage.INVALID_RESPONSE)
        SendToMedia.errorForAll(RetryBehavior.ONCE, lookupRequests, requestId)
    }

    /**
     * Check if the requested information is available.
     * If not, send error message to media for the missing information.
     */
    private fun handleLookupResults(lookupResults: Map<String, LookupResult>) {
        this.lookupRequests.forEach { (bearer, mediaRequestObject) ->
            val lookupResult = lookupResults[bearer]
            if (lookupResult == null) {
                val logInfo = LogInfo(bearer, requestId)
                Logger.e(
                    PrivacyFlags.UNDECIDED,
                    logInfo,
                    "Not included in server message.",
                    LoggerTags.RESPONSE,
                    LoggerTags.LOOKUP
                )
                IRC.error(IRCEntry.LOOKUP, IRCMessage.INVALID_RESPONSE)
                SendToMedia.error(logInfo, RetryBehavior.DELAYED, mediaRequestObject.callback)
                return@forEach
            }
            handleSingleLookupResult(bearer, lookupResult, mediaRequestObject)
        }
    }

    /**
     * Check the status for one [LookupResult]
     * and send received information to media
     */
    private fun handleSingleLookupResult(
        bearer: String,
        lookupResult: LookupResult,
        mediaRequestObject: MediaRequestObject
    ) {
        val logInfo = LogInfo(bearer, requestId)

        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "JSON-Status: ${lookupResult.status}",
            LoggerTags.RESPONSE,
            LoggerTags.LOOKUP
        )
        val callback = mediaRequestObject.callback

        val retryAfter = lookupResult.headers?.retryAfter
        val retryBehavior = Config.getBackendPreferredRetryBehavior(lookupResult.status)
        val contentExpiry = Config.getBackendPreferredContentExpiry(lookupResult.status)

        when (lookupResult.status) {
            200 -> {
                if (lookupResult.data != null) {
                    SendToMedia.metadata(
                        logInfo,
                        this.mcc,
                        lookupResult.eTags,
                        lookupResult.data,
                        callback
                    )

                    handleLogo(bearer, lookupResult.data.logo, mediaRequestObject, contentExpiry)
                } else {
                    IRC.error(IRCEntry.LOOKUP, IRCMessage.INVALID_RESPONSE)
                    SendToMedia.error(logInfo, RetryBehavior.DELAYED, callback)
                }
            }

            204 -> SendToMedia.noContent(logInfo, callback, contentExpiry)
            304 -> SendToMedia.notModified(logInfo, callback, contentExpiry)
            202, 400, 422 -> SendToMedia.retryAfterOrBackendPreferences(
                logInfo,
                callback,
                retryAfter,
                retryBehavior,
                contentExpiry
            )

            else -> {
                IRC.error(IRCEntry.LOOKUP, IRCMessage.INVALID_RESPONSE)
                SendToMedia.error(logInfo, RetryBehavior.NONE, callback)
            }
        }
    }

    private fun handleLogo(bearer: String, logoId: Int?, mediaRequestObject: MediaRequestObject, contentExpiry: Long?) {
        val logInfo = LogInfo(bearer, requestId)
        val logoState = when (logoId) {
            null -> "Not available"
            NO_CHANGES -> "No changes"
            else -> "Success - ID: $logoId"
        }
        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Logo: $logoState",
            LoggerTags.RESPONSE,
            LoggerTags.LOOKUP,
            LoggerTags.LOGO
        )
        when (logoId) {
            null -> SendToMedia.deleteLogo(logInfo, mediaRequestObject.callback, contentExpiry)
            NO_CHANGES -> SendToMedia.notModified(logInfo, mediaRequestObject.callback, contentExpiry)
            else -> requestAvailableImage(mediaRequestObject)
        }
    }

    /**
     * Print response information to IRC and Logger.
     * The body can only get read once from the response, so we need it here as extra parameter.
     */
    private fun logResponse(response: Response, body: String?) {
        val logInfo = LogInfo(bearerList, requestId)
        val status = response.code
        val tags = arrayOf(
            LoggerTags.RESPONSE,
            LoggerTags.LOOKUP
        )

        if (status !in arrayOf(200, 202, 204, 207, 304)) {
            IRC.error(IRCEntry.LOOKUP, status)
        }

        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Status: $status", tags = tags)
        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Header: ${response.headers}", tags = tags)
        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Body: $body", tags = tags)
    }
}