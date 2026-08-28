package de.esolutions.connectedradioclient.handlers.backend.callbacks

import de.esolutions.connectedradioclient.api.GenericMoshiParser
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.backend.registration.result.RegistrationResult
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions.resetRequestConfig
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions.setupRequestConfig
import de.esolutions.connectedradioclient.helper.TestRadioStationDataCallback
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LogoCallbackTest {

    private lateinit var imageCallback: LogoCallback
    private lateinit var testRadioStationDataCallback: TestRadioStationDataCallback
    private lateinit var latch: CountDownLatch
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mockServer: MockWebServer = MockWebServer()
    private lateinit var request: Request
    private var testFilePath: String? = null

    @BeforeEach
    fun before() {
        latch = CountDownLatch(1)
        testRadioStationDataCallback = TestRadioStationDataCallback(latch)
        imageCallback = LogoCallback("test", testRadioStationDataCallback)
        mockServer.start()
        val url = mockServer.url("").toString()
        request = Request.Builder()
            .url(url)
            .get()
            .build()

        val workingDirectory = Paths.get("").toAbsolutePath().toString()
        assertNotNull(workingDirectory)
        testFilePath = "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata"
    }

    @AfterEach
    fun after() {
        mockServer.shutdown()
        ServerDowntimeHandler.serverDowntime = null

        resetRequestConfig()
    }

    @Test
    @DisplayName("receive_200_and_send_logo_to_media[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_and_send_logo_to_media() {
        val image = File("$testFilePath/logo.png").inputStream()
        val mockResponse = MockResponse()
            .setHeader("content-disposition", "filename=\"123456789.png\"")
            .setHeader("content-type", "image/png")
            .setResponseCode(200)
            .setBody(Buffer().readFrom(image))
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus?.retry)
        assertNull(testRadioStationDataCallback.completeStatus?.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions =
            TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(testRadioStationDataCallback.logoETag)
    }

    @Test
    @DisplayName("receive_200_and_send_logo_to_media_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_and_send_logo_to_media_with_request_conf() {
        setupRequestConfig()
        val image = File("$testFilePath/logo.png").inputStream()
        val mockResponse = MockResponse()
            .setHeader("content-disposition", "filename=\"123456789.png\"")
            .setHeader("content-type", "image/png")
            .setResponseCode(200)
            .setBody(Buffer().readFrom(image))
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus?.retry)
        assertNull(testRadioStationDataCallback.completeStatus?.retryDelaySeconds)
        assertNotNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions =
            TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(testRadioStationDataCallback.logoETag)
    }

    @Test
    @DisplayName("receive_200_with_etags_and_send_it_to_media[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-264258#]")
    fun receive_200_with_etags_and_send_it_to_media() {
        val image = File("$testFilePath/logo.png").inputStream()
        val mockResponse = MockResponse()
            .setHeader("content-disposition", "filename=\"123456789.png\"")
            .setHeader("content-type", "image/png")
            .setHeader("ETag", "\"::etag::\"")
            .setResponseCode(200)
            .setBody(Buffer().readFrom(image))
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(1L, TimeUnit.SECONDS)

        assertEquals("\"::etag::\"", testRadioStationDataCallback.logoETag)
    }

    @Test
    @DisplayName("receive_200_and_send_no_data_to_media[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_and_send_no_data_to_media() {
        val mockResponse = MockResponse()
            .setResponseCode(200)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.ONCE, testRadioStationDataCallback.completeStatus?.retry)
        assertEquals(30, testRadioStationDataCallback.completeStatus?.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_200_and_send_no_data_to_media_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_and_send_no_data_to_media_with_request_conf() {
        setupRequestConfig()
        val mockResponse = MockResponse()
            .setResponseCode(200)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.ONCE, testRadioStationDataCallback.completeStatus?.retry)
        assertEquals(60, testRadioStationDataCallback.completeStatus?.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    fun receive_202_and_send_retry_after_to_media() {
        val mockResponse = MockResponse()
            .setResponseCode(202)
            .setHeader("Retry-After", 123)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName(
        "receive_202_and_send_retry_after_to_media_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_202_and_send_retry_after_to_media_with_request_conf() {
        setupRequestConfig()
        val mockResponse = MockResponse()
            .setResponseCode(202)
            .setHeader("Retry-After", 123)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_202_without_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_202_without_retry_after() {
        val mockResponse = MockResponse()
            .setResponseCode(202)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
    }

    @Test
    @DisplayName("receive_202_without_retry_after_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_202_without_retry_after_with_request_conf() {
        setupRequestConfig()
        val mockResponse = MockResponse()
            .setResponseCode(202)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
    }

    @Test
    @DisplayName("receive_304[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_304() {
        mockServer.enqueue(MockResponse().setResponseCode(304))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_304_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_304_with_request_conf() {
        setupRequestConfig()
        mockServer.enqueue(MockResponse().setResponseCode(304))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNotNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    fun receive_unknown_status() {
        val mockResponse = MockResponse().setResponseCode(418)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_unknown_status_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_unknown_status_with_request_conf() {
        setupRequestConfig()
        val mockResponse = MockResponse().setResponseCode(418)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_unknown_status_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_unknown_status_retry_after() {
        val mockResponse = MockResponse().setResponseCode(418).setHeader("Retry-After", 123)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_unknown_status_retry_after_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_unknown_status_retry_after_with_request_conf() {
        setupRequestConfig()
        val mockResponse = MockResponse().setResponseCode(418).setHeader("Retry-After", 123)
        mockServer.enqueue(mockResponse)

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName(
        "receive_503_with_retry_after" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_503_with_retry_after() {
        mockServer.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", 123))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNotNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName(
        "receive_503_with_retry_after_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_503_with_retry_after_with_request_conf() {
        setupRequestConfig()
        mockServer.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", 123))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNotNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_503[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_503() {
        mockServer.enqueue(MockResponse().setResponseCode(503))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_503_with_retry_after_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_503_with_request_conf() {
        setupRequestConfig()
        mockServer.enqueue(MockResponse().setResponseCode(503))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.EXPONENTIAL, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_429_with_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_429_with_retry_after() {
        mockServer.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", 123))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNotNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_429_with_retry_after_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_429_with_retry_after_with_request_conf() {
        setupRequestConfig()
        mockServer.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", 123))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(123, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNotNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_429[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_429() {
        mockServer.enqueue(MockResponse().setResponseCode(429))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_429_with_retry_after_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_429_with_request_conf() {
        setupRequestConfig()
        mockServer.enqueue(MockResponse().setResponseCode(429))

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, testRadioStationDataCallback.calledFunctions)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.EXPONENTIAL, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_error[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_error() {
        mockServer.shutdown()

        client.newCall(request).enqueue(imageCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.logo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }
}