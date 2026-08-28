package de.esolutions.connectedradioclient.handlers.backend.callbacks

import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.DISPOSITION
import de.esolutions.connectedradioclient.data.FILE_TYPE
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.data.extensions.getRequestID
import de.esolutions.connectedradioclient.data.extensions.getRetryAfter
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import de.esolutions.connectedradioclient.handlers.media.SendToMedia
import de.esolutions.connectedradioclient.irc.IRC
import de.esolutions.connectedradioclient.irc.IRCEntry
import de.esolutions.connectedradioclient.irc.IRCMessage
import de.esolutions.connectedradioclient.logger.LogInfo
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response

class LogoCallback(
    private val bearer: String,
    private val callback: IRadioStationDataCallback
) : Callback {

    private lateinit var logInfo: LogInfo

    /**
     * Send error messages to media
     */
    override fun onFailure(call: Call, e: IOException) {
        logInfo = LogInfo(bearer, call.request().getRequestID())

        IRC.error(IRCEntry.EXCEPTIONS, "Logo=${e.javaClass.simpleName}-${e.message}")
        Logger.ex(
            PrivacyFlags.UNDECIDED,
            LogInfo(bearer, call.request().getRequestID()),
            "Logo request failed",
            e,
            LoggerTags.RESPONSE,
            LoggerTags.LOGO
        )
        SendToMedia.error(logInfo, RetryBehavior.DELAYED, callback)
    }

    /**
     * Try to parse the backend data to objects and return the information to media on success.
     */
    override fun onResponse(call: Call, response: Response) {
        logInfo = LogInfo(bearer, response.getRequestID())

        logResponse(response)

        val retryAfter = response.getRetryAfter()
        val retryBehavior = Config.getBackendPreferredRetryBehavior(response.code)
        val contentExpiry = Config.getBackendPreferredContentExpiry(response.code)

        when (response.code) {
            200 -> handleImageResults(response, callback, contentExpiry)
            204 -> SendToMedia.deleteLogo(logInfo, callback, contentExpiry)
            304 -> SendToMedia.notModified(logInfo, callback, contentExpiry)
            429, 503 -> {
                ServerDowntimeHandler.handleResponse(retryAfter)
                SendToMedia.retryAfterOrBackendPreferences(
                    logInfo,
                    callback,
                    retryAfter,
                    retryBehavior,
                    contentExpiry
                )
            }
            else -> SendToMedia.retryAfterOrBackendPreferences(
                logInfo,
                callback,
                retryAfter,
                retryBehavior,
                contentExpiry
            )
        }
    }

    /**
     * Check the body. Send error to media on body = null.
     * Otherwise, read filename and type header and try to create a [StationLogo] object.
     */
    private fun handleImageResults(response: Response, callback: IRadioStationDataCallback, contentExpiry: Long?) {
        val body = response.body?.bytes()

        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Body size: ${body?.size} Byte",
            LoggerTags.RESPONSE,
            LoggerTags.LOGO
        )

        if (body != null) {
            val disposition = response.header(DISPOSITION)
            val type = response.header(FILE_TYPE)

            StationLogo.createFromBackendData(body, disposition, type)?.let {
                val eTag = response.header("ETag")

                SendToMedia.logo(logInfo, eTag, it, callback, contentExpiry)
                return
            }
        }

        IRC.error(IRCEntry.LOGO, IRCMessage.EMPTY_BODY)
        SendToMedia.error(logInfo, RetryBehavior.ONCE, callback)
    }

    /**
     * Print response information to IRC and Logger.
     */
    private fun logResponse(response: Response) {
        val status = response.code
        val tags = arrayOf(
            LoggerTags.RESPONSE,
            LoggerTags.LOGO
        )

        if (status !in arrayOf(200, 202, 204, 304)) {
            IRC.error(IRCEntry.LOGO, status)
        }

        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Status: $status", tags = tags)
        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Header: ${response.headers}", tags = tags)
    }
}