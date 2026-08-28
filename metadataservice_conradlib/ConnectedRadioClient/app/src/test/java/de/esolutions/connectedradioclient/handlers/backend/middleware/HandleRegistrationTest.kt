package de.esolutions.connectedradioclient.handlers.backend.middleware

import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.EndpointConfig
import de.esolutions.connectedradioclient.data.LOGO_VERSION
import de.esolutions.connectedradioclient.data.LOOKUP_VERSION
import de.esolutions.connectedradioclient.data.REQUEST_ID
import de.esolutions.connectedradioclient.data.backend.registration.result.ApiResultObject
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.handlers.backend.exceptions.RegistrationFailureException
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions
import java.io.File
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock

class HandleRegistrationTest {

    private lateinit var handleRegistration: HandleRegistration
    private val timeProvider: TimeProvider = DefaultTimeProvider()
    private val mockServer = MockWebServer()

    @BeforeEach
    fun before() {
        mockServer.start()
        Config.endpointConfig = EndpointConfig(mockServer.url("").toString(), "operationId")

        val mockTokenHandler = mock(TokenHandler::class.java)
        doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        handleRegistration = HandleRegistration(mockTokenHandler)

        TestHelpingFunctions.setHttpToDefault()
        TestHelpingFunctions.setDefaultConfig()
        TestHelpingFunctions.removeAllCookies()
    }

    private fun enqueueSuccessResponse() {
        val workingDirectory = Paths.get("").toAbsolutePath().toString()
        assertNotNull(workingDirectory)
        val testDataPath =
            "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata"
        val registrationJson =
            File("$testDataPath/registrationResponse.json").readText()

        mockServer.enqueue(MockResponse().setBody(registrationJson).setResponseCode(200))
    }

    @AfterEach
    fun after() {
        mockServer.shutdown()
        ServerDowntimeHandler.serverDowntime = null
    }

    @Test
    @DisplayName("start_new_registration_if_cookie_is_not_present[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-229471#]")
    fun start_new_registration_if_cookie_is_not_present() {
        enqueueSuccessResponse()

        handleRegistration.ensureRegistration()
        val request = mockServer.takeRequest(2L, TimeUnit.SECONDS)
        val body = request?.body?.readUtf8()
        val header = request?.headers

        assertNotNull(request)
        assertNotNull(body)
        assertTrue(body!!.contains("\"version\":\""))
        assertTrue(body.contains("\"lookup\":\"${LOOKUP_VERSION}\""))
        assertTrue(body.contains("\"logo\":\"${LOGO_VERSION}\""))
        assertNotNull(header)
        assertEquals("Bearer 123Token", header!!["Authorization"])
        assertNotNull(header[REQUEST_ID])
        assertNotEquals("", header[REQUEST_ID])
    }

    @Test
    @DisplayName("start_new_registration_if_cookie_is_invalid[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-229471#]")
    fun start_new_registration_if_cookie_is_invalid() {
        val privateCookieManager = Http::class.java.getDeclaredField("cookieManager")
        privateCookieManager.isAccessible = true
        val cookieManager = privateCookieManager.get(Http) as CookieManager

        val httpCookie = HttpCookie(Config.requestConfig.sessionCookieName, "42")
        httpCookie.maxAge = 1L
        cookieManager.cookieStore.add(URI(""), httpCookie)
        Thread.sleep(2000L) //wait until cookie is expired

        enqueueSuccessResponse()
        handleRegistration.ensureRegistration()
        val request = mockServer.takeRequest(2L, TimeUnit.SECONDS)
        val body = request?.body?.readUtf8()
        val header = request?.headers

        assertNotNull(request)
        assertNotNull(body)
        assertTrue(body!!.contains("\"version\":\""))
        assertTrue(body.contains("\"lookup\":\"${LOOKUP_VERSION}\""))
        assertTrue(body.contains("\"logo\":\"${LOGO_VERSION}\""))
        assertNotNull(header)
        assertEquals("Bearer 123Token", header!!["Authorization"])
        assertNotNull(header[REQUEST_ID])
        assertNotEquals("", header[REQUEST_ID])
    }

    @Test
    @DisplayName("dont_start_a_registration_if_everything_is_correct[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-229471#]")
    fun dont_start_a_registration_if_everything_is_correct() {
        val privateCookieManager = Http::class.java.getDeclaredField("cookieManager")
        privateCookieManager.isAccessible = true
        val cookieManager = privateCookieManager.get(Http) as CookieManager
        cookieManager.cookieStore.removeAll()
        HandleRegistration.timeLastRegistrationRequest = timeProvider.getCurrentTimeMillis()

        assertEquals(0, cookieManager.cookieStore.cookies.size)

        val httpCookie = HttpCookie(Config.requestConfig.sessionCookieName, "42")
        cookieManager.cookieStore.add(URI(""), httpCookie)

        handleRegistration.ensureRegistration()
        val request = mockServer.takeRequest(2L, TimeUnit.SECONDS)

        assertNull(request)
    }

    @Test
    @DisplayName(
        "isCookiePresentAndValid_false_if_registration_not_performed[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-229471#]"
    )
    fun isCookiePresentAndValid_false_if_registration_not_performed() {
        HandleRegistration.timeLastRegistrationRequest = null

        assertFalse(HandleRegistration.isCookiePresentAndValid())
    }

    @Test
    @DisplayName(
        "isCookiePresentAndValid_true_if_session_cookie_got_disabled[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-229471#]"
    )
    fun isCookiePresentAndValid_true_if_session_cookie_got_disabled() {
        val sessionEnabled = ConfigObject::class.java.getDeclaredField("sessionEnabled")
        sessionEnabled.isAccessible = true
        sessionEnabled.set(Config.requestConfig, false)

        HandleRegistration.timeLastRegistrationRequest = timeProvider.getCurrentTimeMillis()

        assertTrue(HandleRegistration.isCookiePresentAndValid())
    }

    @Test
    @DisplayName(
        "isCookiePresentAndValid_false_if_session_cookie_got_disabled_but_last_regist_is_too_long_ago[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-259345#]"
    )
    fun isCookiePresentAndValid_false_if_session_cookie_got_disabled_but_last_regist_is_too_long_ago() {
        val sessionEnabled = ConfigObject::class.java.getDeclaredField("sessionEnabled")
        sessionEnabled.isAccessible = true
        sessionEnabled.set(Config.requestConfig, false)

        HandleRegistration.timeLastRegistrationRequest = timeProvider.getCurrentTimeMillis() - (86400 * 2 * 1000)

        assertFalse(HandleRegistration.isCookiePresentAndValid())
    }

    @Test
    @DisplayName("ensureRegistration_throws_exception_if_registration_fails" +
                         "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-245637,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun ensureRegistration_throws_exception_if_registration_fails() {
        mockServer.enqueue(MockResponse().setResponseCode(503).addHeader("Retry-After", 123))

        val exception = assertThrows(RegistrationFailureException::class.java) {
            handleRegistration.ensureRegistration()
        }
        assertEquals(123, exception.retryAfter)
        assertEquals(503, exception.responseCode)
    }

    @Test
    @DisplayName("set_registration_result[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256037,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun set_registration_result() {
        val api = ApiResultObject("test", "test")

        val config = ConfigObject(
            0,
            0.0,
            123,
            0,
            false,
            "",
            86400L,
            30L,
            mapOf(),
            mapOf()
        )

        HandleRegistration.setRegistrationResult(api, config)

        Assertions.assertSame(api, Config.apiEndpoints)
        Assertions.assertSame(config, Config.requestConfig)
        assertNotNull(HandleRegistration.timeLastRegistrationRequest)
    }
}