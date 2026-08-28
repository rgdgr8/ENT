package de.esolutions.connectedradioclient.handlers.backend.jobs

import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.EndpointConfig
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import de.esolutions.connectedradioclient.handlers.backend.middleware.ServerDowntimeHandler
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions
import java.io.File
import java.nio.file.Paths
import java.util.*
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class RegistrationRequestJobTest {

    private val mockWebServer = MockWebServer()
    private lateinit var mockTokenHandler: TokenHandler
    private lateinit var registrationRequestJob: RegistrationRequestJob
    private lateinit var testDataPath: String

    @BeforeEach
    fun before() {
        val workingDirectory = Paths.get("").toAbsolutePath().toString()
        assertNotNull(workingDirectory)
        testDataPath =
            "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata"

        TestHelpingFunctions.clearRegistrationResult()
        TestHelpingFunctions.setHttpToDefault()

        mockWebServer.start()
        Config.endpointConfig = EndpointConfig(mockWebServer.url("").toString(), "operationId")

        mockTokenHandler = mock(TokenHandler::class.java)
        registrationRequestJob = RegistrationRequestJob(mockTokenHandler)
    }

    @AfterEach
    fun after() {
        mockWebServer.shutdown()
        ServerDowntimeHandler.serverDowntime = null
    }

    @Test
    @DisplayName(
        "it_sends_the_request_with_correct_headers" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-244019,HCP3_GLOBAL_CLU41:HCPG-265397,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun it_sends_the_request_with_correct_headers() {
        TestHelpingFunctions.removeAllCookies()

        val registrationJson =
            File("$testDataPath/registrationResponse.json").readText()

        mockWebServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))

        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        registrationRequestJob.run()

        val request = mockWebServer.takeRequest()

        assertEquals("Bearer 123Token", request.headers["Authorization"])
        assertEquals("unknown", request.headers["X-Brand"])
        assertEquals("unknown", request.headers["X-Product"])
        assertEquals("unknown", request.headers["X-Cluster"])

        assertEquals("application/json", request.headers["Accept"])
        assertEquals("application/json; charset=utf-8", request.headers["Content-Type"])
        assertEquals(request.bodySize.toString(), request.headers["Content-Length"])
        assertEquals("/register/2.1", request.path)

        TestHelpingFunctions.setDefaultConfig()
    }

    @Test
    fun do_nothing_if_token_is_null() {
        assertFalse(registrationRequestJob.successful)

        registrationRequestJob.run()

        assertFalse(registrationRequestJob.successful)
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    @DisplayName("receive_200_and_set_config_and_success[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_and_set_config_and_success() {
        TestHelpingFunctions.removeAllCookies()
        assertFalse(registrationRequestJob.successful)

        val registrationJson =
            File("$testDataPath/registrationResponse.json").readText()

        mockWebServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))

        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        registrationRequestJob.run()

        assertTrue(registrationRequestJob.successful)
        assertEquals(200, registrationRequestJob.responseCode)
        assertEquals("testCookie", Config.requestConfig.sessionCookieName)
        assertEquals("url/to/lookup", Config.apiEndpoints.lookup)
        assertEquals("url/to/logo", Config.apiEndpoints.logo)

        TestHelpingFunctions.setDefaultConfig()
    }

    @Test
    @DisplayName("receive_200_but_with_no_body[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_200_but_with_no_body() {
        assertFalse(registrationRequestJob.successful)

        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        registrationRequestJob.run()

        assertFalse(registrationRequestJob.successful)
        assertEquals(200, registrationRequestJob.responseCode)
    }

    @Test
    @DisplayName("receive_unknown_status_exception_thrown[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_unknown_status_exception_thrown() {
        assertFalse(registrationRequestJob.successful)

        mockWebServer.enqueue(MockResponse().setResponseCode(123))

        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        registrationRequestJob.run()

        assertFalse(registrationRequestJob.successful)
        assertNull(registrationRequestJob.responseCode)
    }

    @Test
    @DisplayName("receive_503_with_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun receive_503_with_retry_after() {
        assertFalse(registrationRequestJob.successful)

        mockWebServer.enqueue(MockResponse().setResponseCode(503).setHeader("Retry-After", 123))

        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        registrationRequestJob.run()

        assertFalse(registrationRequestJob.successful)
        assertEquals(503, registrationRequestJob.responseCode)
        assertEquals(123, registrationRequestJob.retryAfterResponse)
        assertNotNull(ServerDowntimeHandler.serverDowntime)
    }

    @Test
    @DisplayName(
        "do_nothing_if_server_is_marked_as_down[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637,HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun do_nothing_if_server_is_marked_as_down() {
        assertFalse(registrationRequestJob.successful)

        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        val now = Calendar.getInstance()

        val serverDowntime = Calendar.getInstance()
        serverDowntime.timeInMillis = now.timeInMillis + 123 * 1000
        ServerDowntimeHandler.serverDowntime = serverDowntime

        Mockito.mockStatic(Calendar::class.java).use {
            Mockito.`when`(Calendar.getInstance()).thenReturn(now)

            registrationRequestJob.run()
        }

        assertFalse(registrationRequestJob.successful)
        assertNull(registrationRequestJob.responseCode)
        assertEquals(123, registrationRequestJob.retryAfterResponse)
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    @DisplayName("do_nothing_if_url_is_invalid[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256200,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun do_nothing_if_url_is_invalid() {
        Mockito.doAnswer { "token" }.`when`(mockTokenHandler).getToken("operationId")
        Config.endpointConfig = EndpointConfig("invalid_url", "operationId")

        registrationRequestJob.run()

        assertFalse(registrationRequestJob.successful)
        assertNull(registrationRequestJob.responseCode)
        assertEquals(0, mockWebServer.requestCount)
    }

    @Test
    @DisplayName("is_send_seconds_since_last_request_correct[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259345#]")
    fun save_the_current_time_after_first_request() {
        TestHelpingFunctions.removeAllCookies()
        TestHelpingFunctions.clearRegistrationResult()
        assertNull(HandleRegistration.timeLastRegistrationRequest)

        val registrationJson =
            File("$testDataPath/registrationResponse.json").readText()

        mockWebServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")
        registrationRequestJob.run()

        assertNotNull(HandleRegistration.timeLastRegistrationRequest)
    }

    @Test
    @DisplayName("is_send_seconds_since_last_request_correct[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259345#]")
    fun is_send_seconds_since_last_request_correct() {
        TestHelpingFunctions.removeAllCookies()

        val registrationJson =
            File("$testDataPath/registrationResponse.json").readText()

        mockWebServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")
        registrationRequestJob.run()
        var request = mockWebServer.takeRequest()

        assertNull(request.headers["X-Registration-Age"])

        mockWebServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")
        registrationRequestJob.run()
        request = mockWebServer.takeRequest()

        assertNotNull(request.headers["X-Registration-Age"])
    }

    @Test
    @DisplayName("save_current_time_after_every_registration_request[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259345#]")
    fun save_current_time_after_every_registration_request() {
        TestHelpingFunctions.removeAllCookies()
        TestHelpingFunctions.clearRegistrationResult()
        var registrationTime = HandleRegistration.timeLastRegistrationRequest
        assertNull(registrationTime)

        val registrationJson =
            File("$testDataPath/registrationResponse.json").readText()

        mockWebServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")
        registrationRequestJob.run()

        registrationTime = HandleRegistration.timeLastRegistrationRequest
        assertNotNull(registrationTime)

        Thread.sleep(100) //wait bevor the secound request to see if the timestamp changes and raise

        mockWebServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))
        Mockito.doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")
        registrationRequestJob.run()

        assertNotNull(HandleRegistration.timeLastRegistrationRequest)
        assertTrue(registrationTime!! < HandleRegistration.timeLastRegistrationRequest!!)
    }
}
