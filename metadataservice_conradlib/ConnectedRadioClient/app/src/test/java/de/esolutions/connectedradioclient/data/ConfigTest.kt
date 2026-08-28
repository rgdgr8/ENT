package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ConfigTest {

    private fun setupEmpty() {
        Config.requestConfig = ConfigObject(
            batchSize = 20,
            batchDebounce = 0.5,
            batchMaxWait = 2,
            connectionKeepalive = null,
            sessionEnabled = false,
            sessionCookieName = null,
            maxRegistrationAge = 86400L,
            retryDelay = 30L,
            statusCodeContentExpiry = mapOf(),
            statusCodeRetryBehavior = mapOf()
        )
    }

    private fun setupContentExpiryAndRetryBehavior() {
        Config.requestConfig = ConfigObject(
            batchSize = 20,
            batchDebounce = 0.5,
            batchMaxWait = 2,
            connectionKeepalive = null,
            sessionEnabled = false,
            sessionCookieName = null,
            maxRegistrationAge = 86400L,
            retryDelay = 30L,
            statusCodeContentExpiry = mapOf(
                200 to 86400,
                204 to 86400,
                304 to 86400,
                400 to 2419200
            ),
            statusCodeRetryBehavior = mapOf(
                202 to RetryBehavior.DELAYED,
                401 to RetryBehavior.EXPONENTIAL,
                403 to RetryBehavior.EXPONENTIAL,
                429 to RetryBehavior.EXPONENTIAL,
                500 to RetryBehavior.ONCE,
                502 to RetryBehavior.ONCE,
                503 to RetryBehavior.EXPONENTIAL,
                504 to RetryBehavior.ONCE
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        Config.requestConfig = ConfigObject(
            batchSize = 20,
            batchDebounce = 0.5,
            batchMaxWait = 2,
            connectionKeepalive = null,
            sessionEnabled = false,
            sessionCookieName = null,
            maxRegistrationAge = 86400L,
            retryDelay = 30L,
            statusCodeContentExpiry = mapOf(),
            statusCodeRetryBehavior = mapOf()
        )
    }

    @Test
    @DisplayName("get_backend_preferred_content_expiry_empty_map[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun get_backend_preferred_content_expiry_empty_map() {
        // Precondition
        setupEmpty()

        // Map is empty - everything should return null
        // 200's
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(200))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(202))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(204))

        // 300's
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(304))

        // 400's
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(400))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(401))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(403))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(429))

        // 500's
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(500))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(502))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(503))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(504))

        // Other
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(-10))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(0))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(1))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(9999999))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(Int.MAX_VALUE))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(Int.MIN_VALUE))
    }

    @Test
    @DisplayName("get_backend_preferred_content_expiry_filled_map[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun get_backend_preferred_content_expiry_filled_map() {
        // Precondition
        setupContentExpiryAndRetryBehavior()

        // Map is not empty - 200, 204, 304 and 400 should return something
        // 200's
        Assertions.assertEquals(86400, Config.getBackendPreferredContentExpiry(200))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(202))
        Assertions.assertEquals(86400, Config.getBackendPreferredContentExpiry(204))

        // 300's
        Assertions.assertEquals(86400, Config.getBackendPreferredContentExpiry(304))

        // 400's
        Assertions.assertEquals(2419200, Config.getBackendPreferredContentExpiry(400))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(401))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(403))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(429))

        // 500's
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(500))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(502))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(503))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(504))

        // Other
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(-10))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(0))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(1))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(9999999))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(Int.MAX_VALUE))
        Assertions.assertNull(Config.getBackendPreferredContentExpiry(Int.MIN_VALUE))
    }

    @Test
    @DisplayName("get_backend_preferred_retry_behavior_empty_map[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun get_backend_preferred_retry_behavior_empty_map() {
        // Precondition
        setupEmpty()

        // Map is empty - everything should return null
        // 200's
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(200))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(202))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(204))

        // 300's
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(304))

        // 400's
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(400))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(401))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(403))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(429))

        // 500's
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(500))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(502))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(503))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(504))

        // Other
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(-10))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(0))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(1))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(9999999))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(Int.MAX_VALUE))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(Int.MIN_VALUE))
    }

    @Test
    @DisplayName("get_backend_preferred_retry_behavior_filled_map[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun get_backend_preferred_retry_behavior_filled_map() {
        // Precondition
        setupContentExpiryAndRetryBehavior()

        // Map is not empty - 202, 401, 403, 429, 500, 502, 503 and 504 should return something
        // 200's
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(200))
        Assertions.assertEquals(RetryBehavior.DELAYED, Config.getBackendPreferredRetryBehavior(202))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(204))

        // 300's
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(304))

        // 400's
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(400))
        Assertions.assertEquals(RetryBehavior.EXPONENTIAL, Config.getBackendPreferredRetryBehavior(401))
        Assertions.assertEquals(RetryBehavior.EXPONENTIAL, Config.getBackendPreferredRetryBehavior(403))
        Assertions.assertEquals(RetryBehavior.EXPONENTIAL, Config.getBackendPreferredRetryBehavior(429))

        // 500's
        Assertions.assertEquals(RetryBehavior.ONCE, Config.getBackendPreferredRetryBehavior(500))
        Assertions.assertEquals(RetryBehavior.ONCE, Config.getBackendPreferredRetryBehavior(502))
        Assertions.assertEquals(RetryBehavior.EXPONENTIAL, Config.getBackendPreferredRetryBehavior(503))
        Assertions.assertEquals(RetryBehavior.ONCE, Config.getBackendPreferredRetryBehavior(504))

        // Other
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(-10))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(0))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(1))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(9999999))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(Int.MAX_VALUE))
        Assertions.assertEquals(RetryBehavior.NONE, Config.getBackendPreferredRetryBehavior(Int.MIN_VALUE))
    }
}