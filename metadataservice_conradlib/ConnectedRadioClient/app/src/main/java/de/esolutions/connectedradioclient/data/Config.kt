package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.registration.result.ApiResultObject
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.enums.RetryBehavior

data object Config {

    lateinit var endpointConfig: EndpointConfig
    lateinit var apiEndpoints: ApiResultObject

    /**
     * This config object will be overwritten by the registration request,
     * but we need a default for the first request.
     */
    var requestConfig: ConfigObject = ConfigObject(
        batchSize = 20,
        batchDebounce = 0.5,
        batchMaxWait = 2,
        connectionKeepalive = null,
        sessionEnabled = false,
        sessionCookieName = null,
        maxRegistrationAge = 86400L,
        retryDelay = 30L,
        statusCodeContentExpiry = mapOf(),
        statusCodeRetryBehavior = mapOf(
            401 to RetryBehavior.EXPONENTIAL,
            403 to RetryBehavior.EXPONENTIAL,
            429 to RetryBehavior.EXPONENTIAL,
            500 to RetryBehavior.EXPONENTIAL,
            502 to RetryBehavior.EXPONENTIAL,
            503 to RetryBehavior.EXPONENTIAL,
            504 to RetryBehavior.EXPONENTIAL
        )
    )

    /**
     * Retrieves the backend preferred retry behavior for a given HTTP response code from the request configuration.
     * If the response code is not found in the [ConfigObject.statusCodeRetryBehavior] map,
     * the method returns [RetryBehavior.NONE] as the default behavior.
     *
     * @param responseCode The HTTP response code for which the retry behavior is requested.
     * @return The preferred retry behavior for the given response code.
     */
    fun getBackendPreferredRetryBehavior(
        responseCode: Int,
        default: RetryBehavior = RetryBehavior.NONE
    ): RetryBehavior {
        return requestConfig.statusCodeRetryBehavior[responseCode] ?: default
    }

    /**
     * Retrieves the backend preferred content expiry for a given HTTP response code from the request configuration.
     * If the response code is not found in the [ConfigObject.statusCodeRetryBehavior] map,
     * the method returns null as the default behavior.
     *
     * @param responseCode The HTTP response code for which the content expiry is requested.
     * @return The preferred content expiry for the given response code, or null if not found.
     */
    fun getBackendPreferredContentExpiry(responseCode: Int): Long? {
        return requestConfig.statusCodeContentExpiry[responseCode]
    }
}