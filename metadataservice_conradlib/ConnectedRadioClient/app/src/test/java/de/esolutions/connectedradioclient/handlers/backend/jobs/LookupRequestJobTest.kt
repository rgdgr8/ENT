package de.esolutions.connectedradioclient.handlers.backend.jobs

import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.EndpointConfig
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.REQUEST_ID
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.data.bearer.IBearer
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.handlers.backend.exceptions.RegistrationFailureException
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions.resetRequestConfig
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions.setupRequestConfig
import de.esolutions.connectedradioclient.helper.TestRadioStationDataCallback
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.mockito.Mockito.mock

class LookupRequestJobTest {

    private val mockWebServer = MockWebServer()
    private lateinit var mockTokenHandler: TokenHandler
    private lateinit var mockRegistration: HandleRegistration
    private lateinit var testRadioStationDataCallback: TestRadioStationDataCallback
    private val latch = CountDownLatch(1)
    private var testFilePath: String? = null

    @BeforeEach
    fun before() {
        TestHelpingFunctions.setHttpToDefault()
        mockWebServer.start()
        Config.endpointConfig = EndpointConfig(mockWebServer.url("").toString(), "operationId")

        TestHelpingFunctions.setDefaultConfig()

        mockTokenHandler = mock(TokenHandler::class.java)
        mockRegistration = mock(HandleRegistration::class.java)

        testRadioStationDataCallback = TestRadioStationDataCallback(latch)

        val workingDirectory = Paths.get("").toAbsolutePath().toString()
        Assertions.assertNotNull(workingDirectory)
        testFilePath = "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata"
    }

    private fun createLookupRequestJob(
        bearer: IBearer = FmBearer("mcc", "pi", "frequency"),
        eTags: ETags = ETags(null, null, null, null, null),
        callback: IRadioStationDataCallback = testRadioStationDataCallback
    ): LookupRequestJob {
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)

        return LookupRequestJob(
            mapOf(bearer.getBearerString() to mediaRequestObject),
            mediaRequestObject.mcc,
            mockRegistration,
            mockTokenHandler
        )
    }

    @AfterEach
    fun after() {
        mockWebServer.shutdown()
        ServerDowntimeHandler.serverDowntime = null
        resetRequestConfig()
    }

    @Test
    @DisplayName("send_request_to_server[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-244019,HCP3_GLOBAL_CLU41:HCPG-265397#]")
    fun send_request_to_server() {
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        createLookupRequestJob().run()

        val request = mockWebServer.takeRequest(2L, TimeUnit.SECONDS)
        val body = request?.body?.readUtf8()
        val url = request?.requestUrl?.toString()
        val header = request?.headers

        assertEquals("{\"fm:?.pi.frequency\":{\"etags\":{}}}", body)
        assertTrue(url!!.contains("/lookup"))
        assertEquals("Bearer 123Token", header!!["Authorization"])
        assertEquals("mcc", header["X-Mcc"])
        assertEquals("unknown", header["X-Brand"])
        assertEquals("unknown", header["X-Product"])
        assertEquals("unknown", header["X-Cluster"])
        assertNotEquals("", header[REQUEST_ID])

        assertEquals("application/json", request.headers["Accept"])
        assertEquals("application/json; charset=utf-8", request.headers["Content-Type"])
        assertEquals(request.bodySize.toString(), request.headers["Content-Length"])
    }

    @Test
    fun send_error_to_media_if_token_is_null() {
        createLookupRequestJob().run()
        latch.await(1L, TimeUnit.SECONDS)

        assertEquals(
            TestRadioStationDataCallback.COMPLETE,
            testRadioStationDataCallback.calledFunctions
        )
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_server_is_marked_as_down" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_server_is_marked_as_down() {
        val now = Calendar.getInstance()

        val serverDowntime = Calendar.getInstance()
        serverDowntime.timeInMillis = now.timeInMillis + 123 * 1000
        ServerDowntimeHandler.serverDowntime = serverDowntime

        Mockito.mockStatic(Calendar::class.java).use {
            Mockito.`when`(Calendar.getInstance()).thenReturn(now)

            createLookupRequestJob().run()
        }

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_server_is_marked_as_down_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_server_is_marked_as_down_with_request_conf() {
        setupRequestConfig()
        val now = Calendar.getInstance()

        val serverDowntime = Calendar.getInstance()
        serverDowntime.timeInMillis = now.timeInMillis + 123 * 1000
        ServerDowntimeHandler.serverDowntime = serverDowntime

        Mockito.mockStatic(Calendar::class.java).use {
            Mockito.`when`(Calendar.getInstance()).thenReturn(now)

            createLookupRequestJob().run()
        }

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_retry_after_set" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_retry_after_set() {
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(500, 123)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_no_retry_after" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_no_retry_after() {
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(500, null)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.EXPONENTIAL, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_retry_after_set_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_retry_after_set_with_request_conf() {
        setupRequestConfig()
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(500, 123)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_no_retry_after_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_no_retry_after_with_request_conf() {
        setupRequestConfig()
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(500, null)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.ONCE, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_400_no_retry_after_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_400_no_retry_after_with_request_conf() {
        setupRequestConfig()
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(400, null)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.EXPONENTIAL, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        Assertions.assertNotNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_400_retry_after_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_400_retry_after_with_request_conf() {
        setupRequestConfig()
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(400, 123)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        Assertions.assertNotNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_400_retry_after" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_400_retry_after() {
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(400, 123)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_after_or_backend_preferences_if_registration_fails_400" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_after_or_backend_preferences_if_registration_fails_400() {
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(400, null)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.EXPONENTIAL, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_delayed_if_registration_fails_without_info" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_delayed_if_registration_fails_without_info() {
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(null, null)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName(
        "retry_delayed_if_registration_fails_without_info_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun retry_delayed_if_registration_fails_without_info_with_request_conf() {
        setupRequestConfig()
        BDDMockito.given(mockRegistration.ensureRegistration()).willAnswer {
            throw RegistrationFailureException(null, null)
        }

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
    }

    @Test
    @DisplayName("send_no_mcc_header_if_no_mcc_exists[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-250516#]")
    fun send_no_mcc_header_if_no_mcc_exists() {
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        val bearer = HdBearer.createFromString("hd:040.00066.1")!!
        val callback = mock(IRadioStationDataCallback::class.java)

        createLookupRequestJob(bearer, callback = callback).run()

        val request = mockWebServer.takeRequest(2L, TimeUnit.SECONDS)

        assertNull(request!!.headers["X-Mcc"])
    }

    @Test
    @DisplayName(
        "send_error_to_media_if_url_is_invalid[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256200,HCP3_GLOBAL_CLU41:HCPG-263702#]"
    )
    fun send_error_to_media_if_url_is_invalid() {
        Mockito.doAnswer { "token" }.`when`(mockTokenHandler).getToken("operationId")
        Config.endpointConfig = EndpointConfig("invalid_url", "operationId")

        createLookupRequestJob().run()

        assertEquals(0, mockWebServer.requestCount)
        assertEquals(
            TestRadioStationDataCallback.COMPLETE,
            testRadioStationDataCallback.calledFunctions
        )
        assertEquals(RetryBehavior.EXPONENTIAL, testRadioStationDataCallback.completeStatus!!.retry)
    }

    @Test
    @DisplayName("does_send_etag_if_it_is_not_null[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-264258#]")
    fun does_send_etag_if_it_is_not_null() {
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        createLookupRequestJob(
            eTags = ETags(
                "::metadata_etag::",
                "::bearer_etag::",
                "::stream_etag::",
                "::logo_etag::",
                "::logo_small_etag::"
            )
        ).run()

        val request = mockWebServer.takeRequest(1L, TimeUnit.SECONDS)
        val body = request?.body?.readUtf8()!!

        assertTrue(body.contains("\"metadata\":\"::metadata_etag::\""))
        assertTrue(body.contains("\"bearers\":\"::bearer_etag::\""))
        assertTrue(body.contains("\"streams\":\"::stream_etag::\""))
        assertTrue(body.contains("\"logo\":\"::logo_etag::\""))
        assertTrue(body.contains("\"logo_small\":\"::logo_small_etag::\""))
    }

    /**
     * To avoid sending unnecessary data, we should send the eTags only when they are set.
     */
    @Test
    @DisplayName("does_not_send_etag_if_it_is_null[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-264258#]")
    fun does_not_send_etag_if_it_is_null() {
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        createLookupRequestJob().run()

        val request = mockWebServer.takeRequest(1L, TimeUnit.SECONDS)
        val body = request?.body?.readUtf8()!!

        // Moshi should generate an empty eTags object, if all eTags are null.
        assertTrue(body.contains("\"etags\":{}}"))
    }
}