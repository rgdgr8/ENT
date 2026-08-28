package de.esolutions.connectedradioclient.handlers.backend.jobs

import de.esolutions.connectedradioclient.BuildConfig
import de.esolutions.connectedradioclient.api.GenericMoshiParser
import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.LOGO_VERSION
import de.esolutions.connectedradioclient.data.LOOKUP_VERSION
import de.esolutions.connectedradioclient.data.REGISTRATION_URL_PATH
import de.esolutions.connectedradioclient.data.REGISTRATION_VERSION
import de.esolutions.connectedradioclient.data.RequestHeader
import de.esolutions.connectedradioclient.data.backend.registration.request.ApiRequestObject
import de.esolutions.connectedradioclient.data.backend.registration.request.RegistrationRequest
import de.esolutions.connectedradioclient.data.backend.registration.result.RegistrationResult
import de.esolutions.connectedradioclient.data.extensions.buildUrl
import de.esolutions.connectedradioclient.data.extensions.getRetryAfter
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.irc.IRC
import de.esolutions.connectedradioclient.irc.IRCEntry
import de.esolutions.connectedradioclient.irc.IRCMessage
import de.esolutions.connectedradioclient.logger.LogInfo
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import okhttp3.Response

class RegistrationRequestJob(private val tokenHandler: TokenHandler) : IJob {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    var successful: Boolean = false
    var responseCode: Int? = null
    var retryAfterResponse: Long? = null

    private val requestID = Http.generateRequestID()
    private val logInfo = LogInfo(null, requestID)

    override fun run() {
        Logger.d(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Checking if request to the registration endpoint is required",
            LoggerTags.REGISTRATION
        )

        if (HandleRegistration.isCookiePresentAndValid()) {
            successful = true
            return
        }

        ServerDowntimeHandler.getRemainingDowntime()?.let {
            retryAfterResponse = it
            return
        }

        val token = tokenHandler.getToken(Config.endpointConfig.operationId)
        if (token == null) {
            IRC.error(IRCEntry.REGISTRATION, IRCMessage.TOKEN)
            return
        }

        val apiRequestObject = ApiRequestObject(LOOKUP_VERSION, LOGO_VERSION)
        val registrationRequest = RegistrationRequest(BuildConfig.VERSION_NAME, apiRequestObject)
        Logger.d(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Start registrationRequest parsing with moshi",
            LoggerTags.REGISTRATION
        )
        val requestJson = GenericMoshiParser.parseToJson(registrationRequest)
        val header = RequestHeader(token, requestID)
        header.accept = "application/json"
        HandleRegistration.timeLastRegistrationRequest?.let {
            header.secondsSinceLastRegistration = (timeProvider.getCurrentTimeMillis() - it) / 1000L
        }

        Logger.i(
            PrivacyFlags.SAFE,
            logInfo,
            "DEBUG: About to build URL from: ${Config.endpointConfig.url}",
            LoggerTags.REGISTRATION
        )

        try {
            val url = Config.endpointConfig.url.buildUrl("$REGISTRATION_URL_PATH/$REGISTRATION_VERSION")

            Logger.i(
                PrivacyFlags.SAFE,
                logInfo,
                "DEBUG: URL built successfully: $url",
                LoggerTags.REGISTRATION
            )

            Logger.i(
                PrivacyFlags.UNDECIDED,
                logInfo,
                "Start request to the registration endpoint",
                LoggerTags.REGISTRATION,
                LoggerTags.REQUEST
            )

            Logger.i(
                PrivacyFlags.SAFE,
                logInfo,
                "DEBUG: About to execute POST",
                LoggerTags.REGISTRATION
            )

            Http.executePost(url, header, requestJson).use { response ->
                Logger.i(
                    PrivacyFlags.SAFE,
                    logInfo,
                    "DEBUG: Got response: ${response.code}",
                    LoggerTags.REGISTRATION
                )
                parseResponse(response)
            }
        } catch (ex: Exception) {
            Logger.i(
                PrivacyFlags.SAFE,
                logInfo,
                "DEBUG: Exception caught: ${ex.javaClass.simpleName} - ${ex.message}",
                LoggerTags.REGISTRATION
            )
            IRC.error(IRCEntry.EXCEPTIONS, "Registration=${ex.javaClass.simpleName}-${ex.message}")
            Logger.ex(
                PrivacyFlags.UNDECIDED,
                logInfo,
                "Registration failure",
                ex,
                LoggerTags.REQUEST,
                LoggerTags.REGISTRATION
            )
        }
    }

    private fun parseResponse(response: Response) {
        val body = response.body?.string()
        logResponse(response, body)
        responseCode = response.code

        when (response.code) {
            200 -> handleSuccessfulResponse(body)
            429, 503 -> handleRetryAfterResponse(response)
        }
    }

    private fun handleSuccessfulResponse(body: String?) {
        val registrationResult = body?.let {
            GenericMoshiParser.parseFromJson<RegistrationResult>(it)
        }
        registrationResult?.let {
            HandleRegistration.setRegistrationResult(it.api, it.config)

            successful = true
            return
        }

        IRC.error(IRCEntry.REGISTRATION, IRCMessage.INVALID_RESPONSE)
        Logger.e(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "No body or invalid json received.",
            LoggerTags.RESPONSE,
            LoggerTags.REGISTRATION
        )
    }

    private fun handleRetryAfterResponse(response: Response) {
        response.getRetryAfter()?.let {
            retryAfterResponse = it
            ServerDowntimeHandler.setServerDowntime(it)
        }
    }

    /**
     * Print response information to IRC and Logger.
     * The body can only get read once from the response, so we need it here as extra parameter.
     */
    private fun logResponse(response: Response, body: String?) {
        val status = response.code
        val tags = arrayOf(
            LoggerTags.RESPONSE,
            LoggerTags.REGISTRATION
        )

        if (status != 200) {
            IRC.error(IRCEntry.REGISTRATION, status)
        }

        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Status: $status", tags = tags)
        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Header: ${response.headers}", tags = tags)
        Logger.i(PrivacyFlags.UNDECIDED, logInfo, "Body: $body", tags = tags)
    }
}
