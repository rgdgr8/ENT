package de.esolutions.connectedradioclient.handlers.backend.middleware

import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.backend.registration.result.ApiResultObject
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.handlers.backend.exceptions.RegistrationFailureException
import de.esolutions.connectedradioclient.handlers.backend.jobs.RegistrationRequestJob
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration.Companion.isCookiePresentAndValid
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags

class HandleRegistration(private val tokenHandler: TokenHandler) {

    /**
     * Checks the registration cookies with [isCookiePresentAndValid], if these are invalid,
     * [startRegistration] is started.
     *
     * @throws RegistrationFailureException
     */
    fun ensureRegistration() {
        if (!isCookiePresentAndValid()) {
            startRegistration()
        }
    }

    /**
     * Executes the RegistrationRequestJob in sync.
     *
     * @throws RegistrationFailureException
     */
    private fun startRegistration() {
        val job = RegistrationRequestJob(tokenHandler)
        job.run()

        if (!job.successful) {
            throw RegistrationFailureException(job.responseCode, job.retryAfterResponse)
        }
    }

    companion object {
        private val timeProvider: TimeProvider = DefaultTimeProvider()
        var timeLastRegistrationRequest: Long? = null // in milliseconds

        /**
         * Check if registration was successful and the cookie is set and valid.
         * Return true if registration data was received and the cookie is disabled.
         * Return true if registration data was received and the cookie is stored and valid.
         * Return false in all other cases.
         */
        fun isCookiePresentAndValid(): Boolean {
            Logger.i(PrivacyFlags.SAFE, null, "Check for valid session cookie", LoggerTags.COOKIE)

            if (timeLastRegistrationRequest == null) {
                Logger.i(
                    PrivacyFlags.SAFE,
                    null,
                    "Registration request not performed yet, session cookie will be ignored",
                    LoggerTags.REGISTRATION,
                    LoggerTags.COOKIE
                )
                return false
            }

            if (!Config.requestConfig.sessionEnabled) {
                val logMessage = "Session cookie is disabled"
                Logger.i(PrivacyFlags.SAFE, null, logMessage, LoggerTags.COOKIE)

                timeLastRegistrationRequest?.let {
                    val timeSinceLastRequest = (timeProvider.getCurrentTimeMillis() - it) / 1000
                    if (timeSinceLastRequest > Config.requestConfig.maxRegistrationAge) {
                        Logger.i(
                            PrivacyFlags.SAFE,
                            null,
                            "Do registration request anyway because last registration was too long ago: " +
                                "$timeSinceLastRequest",
                            LoggerTags.REGISTRATION
                        )
                        return false
                    }
                }

                return true
            }

            val sessionCookieName = Config.requestConfig.sessionCookieName
            if (sessionCookieName == null) {
                Logger.e(
                    PrivacyFlags.SAFE,
                    null,
                    "No session cookie name stored in Config.requestConfig",
                    LoggerTags.REGISTRATION,
                    LoggerTags.COOKIE
                )
                return false
            }

            val cookieValue = Http.getCookie(sessionCookieName)
            if (cookieValue == null) {
                val logMessage = "No session cookie stored: $sessionCookieName"
                Logger.w(PrivacyFlags.UNDECIDED, null, logMessage, LoggerTags.COOKIE)
                return false
            }

            if (cookieValue.hasExpired()) {
                Logger.i(
                    PrivacyFlags.UNDECIDED,
                    null,
                    "Session cookie '$sessionCookieName' is expired",
                    LoggerTags.COOKIE
                )
                return false
            }

            Logger.i(
                PrivacyFlags.SESSION,
                null,
                "Session cookie '$sessionCookieName' is valid: ${cookieValue.value}",
                LoggerTags.COOKIE
            )
            return true
        }

        /**
         * Set the api and requestConfig variable, when the registration result gets delivered.
         */
        fun setRegistrationResult(api: ApiResultObject, requestConfig: ConfigObject) {
            Config.apiEndpoints = api
            Config.requestConfig = requestConfig
            timeLastRegistrationRequest = timeProvider.getCurrentTimeMillis()

            requestConfig.connectionKeepalive?.let { keepAlive ->
                Http.setKeepAlive(keepAlive)
            }
        }
    }
}
