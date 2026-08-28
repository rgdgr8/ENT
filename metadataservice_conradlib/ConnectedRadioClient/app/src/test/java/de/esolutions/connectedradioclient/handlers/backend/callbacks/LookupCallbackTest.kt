package de.esolutions.connectedradioclient.handlers.backend.callbacks

import de.esolutions.connectedradioclient.api.GenericMoshiParser
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.ProviderIdentifier
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.backend.registration.result.RegistrationResult
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class LookupCallbackTest {

    private lateinit var lookupCallback: LookupCallback
    private lateinit var testRadioStationDataCallback: TestRadioStationDataCallback
    private lateinit var latch: CountDownLatch
    private val client: OkHttpClient = OkHttpClient.Builder().build()
    private val mockServer: MockWebServer = MockWebServer()
    private lateinit var request: Request
    private var testFilePath: String? = null
    private var testRegFilePath: String? = null

    // return value for the imageRequestCallback
    private var imageId: String? = null
    private var imageCallback: IRadioStationDataCallback? = null
    private var imageProviderIdentifiers: List<ProviderIdentifier>? = null

    @BeforeEach
    fun before() {
        imageId = null
        imageCallback = null
        imageProviderIdentifiers = null
        latch = CountDownLatch(1)
        testRadioStationDataCallback = TestRadioStationDataCallback(latch)
        val bearer = FmBearer("mcc", "d318", "10130")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, testRadioStationDataCallback)
        lookupCallback =
            LookupCallback(
                mapOf(bearer.getBearerString() to mediaRequestObject),
                "mcc"
            ) {
                run {
                    this.imageId = it.bearer.getBearerString()
                    this.imageCallback = it.callback
                    latch.countDown()
                }
            }
        mockServer.start()
        val url = mockServer.url("").toString()
        request = Request.Builder()
            .url(url)
            .get()
            .build()

        val workingDirectory = Paths.get("").toAbsolutePath().toString()
        testFilePath =
            "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata/lookupCallbackTest"

        testRegFilePath = "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata"
    }

    @AfterEach
    fun after() {
        mockServer.shutdown()
        ServerDowntimeHandler.serverDowntime = null
        resetRequestConfig()
    }

    @Test
    @DisplayName("receive_200_or_207_and_send_data_to_media_without_logo[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_or_207_and_send_data_to_media_without_logo() {
        val lookupJson = File("$testFilePath/validBearer.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_data_to_media_without_logo()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_data_to_media_without_logo()
    }

    @Test
    @DisplayName(
        "receive_200_or_207_and_send_data_to_media_without_logo_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_or_207_and_send_data_to_media_without_logo_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/validBearer.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_data_to_media_without_logo(true)

        // Reset
        testRadioStationDataCallback.expirationDate = null

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_data_to_media_without_logo()
    }

    private fun check_200_or_207_and_send_data_to_media_without_logo(useExpDate: Boolean = false) {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.metadata)
        assertNotNull(testRadioStationDataCallback.bearerList)
        assertNotNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)

        if (useExpDate) {
            assertNotNull(testRadioStationDataCallback.expirationDate)
        } else {
            assertNull(testRadioStationDataCallback.expirationDate)
        }

        val expectedCallFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName(
        "receive_200_and_send_to_media_without_bearer_and_streams" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-264258,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_and_send_to_media_without_bearer_and_streams() {
        val lookupJson = File("$testFilePath/validBearerWithoutStreamsBearers.json").readText()
        createAndAddStringResponseToMockServer(lookupJson, 200)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)

        assertNotNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)

        assertEquals("::metadata_etag::", testRadioStationDataCallback.metadataETag)
        assertNull(testRadioStationDataCallback.bearerETag)
        assertNull(testRadioStationDataCallback.streamETag)

        assertEquals(
            TestRadioStationDataCallback.METADATA or TestRadioStationDataCallback.COMPLETE,
            testRadioStationDataCallback.calledFunctions
        )
    }

    @Test
    @DisplayName(
        "receive_200_and_send_to_media_without_bearer_and_streams_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-264258,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_and_send_to_media_without_bearer_and_streams_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/validBearerWithoutStreamsBearers.json").readText()
        createAndAddStringResponseToMockServer(lookupJson, 200)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNotNull(testRadioStationDataCallback.expirationDate)

        assertNotNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)

        assertEquals("::metadata_etag::", testRadioStationDataCallback.metadataETag)
        assertNull(testRadioStationDataCallback.bearerETag)
        assertNull(testRadioStationDataCallback.streamETag)

        assertEquals(
            TestRadioStationDataCallback.METADATA or TestRadioStationDataCallback.COMPLETE,
            testRadioStationDataCallback.calledFunctions
        )
    }

    @Test
    @DisplayName(
        "receive_200_with_etags_and_send_them_to_media" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-264258,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_with_etags_and_send_them_to_media() {
        val lookupJson = File("$testFilePath/validBearer.json").readText()
        createAndAddStringResponseToMockServer(lookupJson, 200)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)

        assertEquals("::metadata_etag::", testRadioStationDataCallback.metadataETag)
        assertEquals("::stream_etag::", testRadioStationDataCallback.streamETag)
        assertEquals("::bearer_etag::", testRadioStationDataCallback.bearerETag)
    }

    @Test
    @DisplayName(
        "receive_200_with_etags_and_send_them_to_media_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-264258,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_with_etags_and_send_them_to_media_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/validBearer.json").readText()
        createAndAddStringResponseToMockServer(lookupJson, 200)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNotNull(testRadioStationDataCallback.expirationDate)

        assertEquals("::metadata_etag::", testRadioStationDataCallback.metadataETag)
        assertEquals("::stream_etag::", testRadioStationDataCallback.streamETag)
        assertEquals("::bearer_etag::", testRadioStationDataCallback.bearerETag)
    }

    @Test
    fun receive_200_or_207_and_start_request_for_logo() {
        val lookupJson = File("$testFilePath/bearerWithLogo.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_start_request_for_logo()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_start_request_for_logo()
    }

    private fun check_200_or_207_and_start_request_for_logo() {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(imageId)
        assertNotNull(imageCallback)
        assertEquals("fm:?.d318.10130", imageId)
        assertEquals(testRadioStationDataCallback, imageCallback)
    }

    @Test
    @DisplayName("receive_200_or_207_and_delete_logos[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_or_207_and_delete_logos() {
        val lookupJson = File("$testFilePath/deleteLogos.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_delete_logos()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_delete_logos()
    }

    @Test
    @DisplayName("receive_200_or_207_and_delete_logos_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_or_207_and_delete_logos_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/deleteLogos.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_delete_logos(true)

        // Reset
        testRadioStationDataCallback.expirationDate = null

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_delete_logos()
    }

    private fun check_200_or_207_and_delete_logos(useExpDate: Boolean = false) {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNotNull(testRadioStationDataCallback.metadata)
        assertNotNull(testRadioStationDataCallback.bearerList)
        assertNotNull(testRadioStationDataCallback.streamList)
        assertNull(testRadioStationDataCallback.logo)
        assertNull(testRadioStationDataCallback.sLogo)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)

        if (useExpDate) {
            assertNotNull(testRadioStationDataCallback.expirationDate)
        } else {
            assertNull(testRadioStationDataCallback.expirationDate)
        }

        val expectedCallFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    fun receive_200_or_207_and_send_no_data_to_media() {
        val lookupJson = File("$testFilePath/invalidBearer.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_no_data_to_media()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_no_data_to_media()
    }

    @Test
    @DisplayName(
        "receive_200_or_207_and_send_no_data_to_media_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_or_207_and_send_no_data_to_media_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/invalidBearer.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_no_data_to_media()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_no_data_to_media()
    }

    private fun check_200_or_207_and_send_no_data_to_media() {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNotNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)

        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    fun receive_200_or_207_and_send_error_to_media() {
        createAndAddStringResponseToMockServer("", 200)
        check_200_or_207_and_send_error_to_media()

        createAndAddStringResponseToMockServer("", 207)
        check_200_or_207_and_send_error_to_media()
    }

    @Test
    @DisplayName(
        "receive_200_or_207_and_send_error_to_media_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_or_207_and_send_error_to_media_with_request_conf() {
        setupRequestConfig()
        createAndAddStringResponseToMockServer("", 200)
        check_200_or_207_and_send_error_to_media(60)

        createAndAddStringResponseToMockServer("", 207)
        check_200_or_207_and_send_error_to_media(60)
    }

    private fun check_200_or_207_and_send_error_to_media(retryDelay: Long = 30) {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(retryDelay, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.ONCE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)

        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    fun receive_200_or_207_and_found_unknown_bearer() {
        val lookupJson = File("$testFilePath/unknownBearer.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_found_unknown_bearer()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_found_unknown_bearer()
    }

    @Test
    @DisplayName(
        "receive_200_or_207_and_found_unknown_bearer_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_or_207_and_found_unknown_bearer_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/unknownBearer.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_found_unknown_bearer(60)

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_found_unknown_bearer(60)
    }

    private fun check_200_or_207_and_found_unknown_bearer(retryDelay: Long = 30) {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(retryDelay, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    fun receive_200_or_207_and_found_202() {
        val lookupJson = File("$testFilePath/202.json").readText()
        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_found_202()
        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_found_202()
    }

    @Test
    @DisplayName(
        "receive_200_or_207_and_found_202_with_request_conf" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_200_or_207_and_found_202_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/202.json").readText()
        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_found_202()
        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_found_202()
    }

    private fun check_200_or_207_and_found_202() {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)
        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(300L, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_200_or_207_and_send_no_content[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_or_207_and_send_no_content() {
        val lookupJson = File("$testFilePath/noContent.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_no_content()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_no_content()
    }

    @Test
    @DisplayName("receive_200_or_207_and_send_no_content_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_or_207_and_send_no_content_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/noContent.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_no_content(true)

        // Reset
        testRadioStationDataCallback.expirationDate = null

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_no_content()
    }

    private fun check_200_or_207_and_send_no_content(useExpDate: Boolean = false) {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.logo)
        assertNull(testRadioStationDataCallback.sLogo)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)

        if (useExpDate) {
            assertNotNull(testRadioStationDataCallback.expirationDate)
        } else {
            assertNull(testRadioStationDataCallback.expirationDate)
        }

        val expectedCallFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_200_or_207_and_send_not_modified[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_or_207_and_send_not_modified() {
        val lookupJson = File("$testFilePath/notModified.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_not_modified()

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_not_modified()
    }

    @Test
    @DisplayName("receive_200_or_207_and_send_not_modified_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_or_207_and_send_not_modified_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/notModified.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 200)
        check_200_or_207_and_send_not_modified(true)

        // Reset
        testRadioStationDataCallback.expirationDate = null

        createAndAddStringResponseToMockServer(lookupJson, 207)
        check_200_or_207_and_send_not_modified()
    }

    private fun check_200_or_207_and_send_not_modified(useExpDate: Boolean = false) {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)

        if (useExpDate) {
            assertNotNull(testRadioStationDataCallback.expirationDate)
        } else {
            assertNull(testRadioStationDataCallback.expirationDate)
        }

        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    fun receive_202() {
        createAndAddStringResponseToMockServer("", 202, mapOf("Retry-After" to "300"))
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)
        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(300L, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_202_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_202_with_request_conf() {
        setupRequestConfig()
        createAndAddStringResponseToMockServer("", 202, mapOf("Retry-After" to "300"))
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)
        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(300L, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_202_without_header[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_202_without_header() {
        createAndAddStringResponseToMockServer("", 202)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)
        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(30L, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_202_without_header_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_202_without_header_with_request_conf() {
        setupRequestConfig()
        createAndAddStringResponseToMockServer("", 202)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)
        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_204[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_204() {
        createAndAddStringResponseToMockServer("", 204)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.logo)
        assertNull(testRadioStationDataCallback.sLogo)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_204_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_204_with_request_conf() {
        setupRequestConfig()
        createAndAddStringResponseToMockServer("", 204)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.logo)
        assertNull(testRadioStationDataCallback.sLogo)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNotNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_304[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_304() {
        createAndAddStringResponseToMockServer("", 304)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_304_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_304_with_request_conf() {
        setupRequestConfig()
        createAndAddStringResponseToMockServer("", 304)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertNull(testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNotNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    fun receive_400_or_404() {
        createAndAddStringResponseToMockServer("", 400)
        check_400_or_404()

        createAndAddStringResponseToMockServer("", 404)
        check_400_or_404()
    }

    @Test
    @DisplayName("receive_400_or_404_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_400_or_404_with_request_conf() {
        setupRequestConfig()
        createAndAddStringResponseToMockServer("", 400)
        check_400_or_404(true, 60)

        // Reset
        testRadioStationDataCallback.expirationDate = null

        createAndAddStringResponseToMockServer("", 404)
        check_400_or_404(retryDelay = 60)
    }

    private fun check_400_or_404(useExpDate: Boolean = false, retryDelay: Long = 30) {
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(retryDelay, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)

        if (useExpDate) {
            assertNotNull(testRadioStationDataCallback.expirationDate)
        } else {
            assertNull(testRadioStationDataCallback.expirationDate)
        }

        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_503_with_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637#]")
    fun receive_503_with_retry_after() {
        mockServer.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", 123))

        client.newCall(request).enqueue(lookupCallback)
        latch.await(2L, TimeUnit.SECONDS)

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
    @DisplayName("receive_503_with_retry_after_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_503_with_retry_after_with_request_conf() {
        setupRequestConfig()
        mockServer.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", 123))

        client.newCall(request).enqueue(lookupCallback)
        latch.await(2L, TimeUnit.SECONDS)

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

        client.newCall(request).enqueue(lookupCallback)
        latch.await(2L, TimeUnit.SECONDS)

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
    @DisplayName("receive_503_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_503_with_request_conf() {
        setupRequestConfig()
        mockServer.enqueue(MockResponse().setResponseCode(503))

        client.newCall(request).enqueue(lookupCallback)
        latch.await(2L, TimeUnit.SECONDS)

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
    fun receive_unknown_status() {
        createAndAddStringResponseToMockServer("", 418)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
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
        createAndAddStringResponseToMockServer("", 418)
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_error[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_error() {
        mockServer.shutdown()
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_error_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_error_with_request_conf() {
        setupRequestConfig()
        mockServer.shutdown()
        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.DELAYED, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_207_and_found_400[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_207_and_found_400() {
        val lookupJson = File("$testFilePath/400.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 207)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName("receive_207_and_found_400_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_207_and_found_400_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/400.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 207)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNotNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
        assertNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName(
        "receive_207_and_invalid_status_in_json" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-251914,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun receive_207_and_invalid_status_in_json() {
        val lookupJson = File("$testFilePath/invalidStatus.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 207)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(30, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    @Test
    @DisplayName("receive_207_and_invalid_status_in_json_with_request_conf[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_207_and_invalid_status_in_json_with_request_conf() {
        setupRequestConfig()
        val lookupJson = File("$testFilePath/invalidStatus.json").readText()

        createAndAddStringResponseToMockServer(lookupJson, 207)

        client.newCall(request).enqueue(lookupCallback)
        latch.await(10L, TimeUnit.SECONDS)

        assertNull(testRadioStationDataCallback.metadata)
        assertNull(testRadioStationDataCallback.bearerList)
        assertNull(testRadioStationDataCallback.streamList)
        assertNotNull(testRadioStationDataCallback.completeStatus)
        assertEquals(60, testRadioStationDataCallback.completeStatus!!.retryDelaySeconds)
        assertEquals(RetryBehavior.NONE, testRadioStationDataCallback.completeStatus!!.retry)
        assertNull(testRadioStationDataCallback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, testRadioStationDataCallback.calledFunctions)
    }

    private fun createAndAddStringResponseToMockServer(
        body: String,
        statusCode: Int,
        headers: Map<String, String> = mapOf()
    ) {
        val mockResponse = MockResponse()
            .setBody(body)
            .setResponseCode(statusCode)
        headers.forEach { (name, value) -> mockResponse.setHeader(name, value) }
        mockServer.enqueue(mockResponse)
    }
}