package de.esolutions.connectedradioclient.data.backend.registration.result

import com.squareup.moshi.Json
import de.esolutions.connectedradioclient.data.enums.RetryBehavior

@Suppress("LongParameterList")
data class ConfigObject(
    @Json(name = "batch_size") val batchSize: Int,
    @Json(name = "batch_debounce") val batchDebounce: Double,
    @Json(name = "batch_max_wait") val batchMaxWait: Int,
    @Json(name = "connection_keepalive") val connectionKeepalive: Long? = null,
    @Json(name = "session_enabled") val sessionEnabled: Boolean = true,
    @Json(name = "session_cookie_name") val sessionCookieName: String?,
    @Json(name = "max_registration_age") val maxRegistrationAge: Long = 60L * 60L * 24L, // in seconds
    /**
     * Delay in seconds after the bearer should be requested again by the metadata service,
     * in case of an error or retry after and no other delay was specified by the server.
     */
    @Json(name = "retry_delay") val retryDelay: Long = 30L, // in seconds
    @Json(name = "cache_expirations") val statusCodeContentExpiry: Map<Int, Long>,
    @Json(name = "retry_behaviors") val statusCodeRetryBehavior: Map<Int, RetryBehavior>,
)