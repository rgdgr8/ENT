package de.esolutions.connectedradioclient.handlers.backend.controller

import android.content.Context
import android.net.ConnectivityManager
import de.esolutions.connectedradioclient.helper.TestRadioStationDataCallback
import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.EndpointConfig
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.bearer.DabBearer
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import java.lang.reflect.Field
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI

class RequestControllerTest {

    private lateinit var requestController: RequestController
    private lateinit var privateRequestContainer: Field
    private val mockServer = MockWebServer()
    private lateinit var mockTokenHandler: TokenHandler
    private lateinit var mockContext: Context
    private lateinit var cookieManager: CookieManager
    private lateinit var mockConnectivityManager: ConnectivityManager

    @BeforeEach
    fun before() {
        mockServer.start()
        Config.endpointConfig = EndpointConfig(mockServer.url("").toString(), "operationId")

        mockContext = mock(Context::class.java)
        mockConnectivityManager = mock(ConnectivityManager::class.java)
        doAnswer { mockConnectivityManager }
            .`when`(mockContext).getSystemService(Context.CONNECTIVITY_SERVICE)
        mockTokenHandler = mock(TokenHandler::class.java)
        doAnswer { "123Token" }.`when`(mockTokenHandler).getToken("operationId")

        requestController = RequestController(mockContext)

        //Methods
        privateRequestContainer =
            requestController::class.java.getDeclaredField("requestContainers")
        privateRequestContainer.isAccessible = true

        //Set token handler to receive tokens
        val privateTokenHandler = requestController::class.java.getDeclaredField("tokenHandler")
        privateTokenHandler.isAccessible = true
        privateTokenHandler.set(requestController, mockTokenHandler)

        //Set Cookie to pass the isCookiePresentAndValid method
        val privateCookieManager = Http::class.java.getDeclaredField("cookieManager")
        privateCookieManager.isAccessible = true
        cookieManager = privateCookieManager.get(Http) as CookieManager
        cookieManager.cookieStore.add(URI(""), HttpCookie("testCookie", "42"))

        //set the api to test always with the same values
        TestHelpingFunctions.setDefaultConfig()
    }

    @AfterEach
    fun after() {
        mockServer.shutdown()
    }

    @Test
    fun create_new_container_on_first_request() {
        val requestContainersBefore = privateRequestContainer.get(requestController) as MutableMap<*, *>
        assertEquals(0, requestContainersBefore.size)

        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(
            DabBearer("mcc", "ecc", "eid", "sid", "scids"),
            eTags,
            TestRadioStationDataCallback()
        )
        requestController.getRadioStationData(mediaRequestObject)

        val requestContainersAfter = privateRequestContainer.get(requestController) as MutableMap<*, *>
        assertEquals(1, requestContainersAfter.size)
    }

    @Test
    fun create_new_container_if_old_is_locked() {
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(
            DabBearer("mcc", "ecc", "eid", "sid", "scids"),
            eTags,
            TestRadioStationDataCallback()
        )

        val containerMap = privateRequestContainer.get(requestController) as MutableMap<*, *>

        requestController.getRadioStationData(mediaRequestObject)
        assertEquals(1, containerMap.size)
        val firstContainer = containerMap.values.first()

        Thread.sleep(600L)

        requestController.getRadioStationData(mediaRequestObject)
        assertEquals(1, containerMap.size)
        val secondContainer = containerMap.values.first()

        assertNotSame(firstContainer, secondContainer)
    }

    @Test
    fun use_same_container_if_not_locked() {
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(
            DabBearer("mcc", "ecc", "eid", "sid", "scids"),
            eTags,
            TestRadioStationDataCallback()
        )

        val containerMap = privateRequestContainer.get(requestController) as MutableMap<*, *>

        requestController.getRadioStationData(mediaRequestObject)
        assertEquals(1, containerMap.size)
        val firstContainer = containerMap.values.first()

        Thread.sleep(400L)

        requestController.getRadioStationData(mediaRequestObject)
        assertEquals(1, containerMap.size)
        val secondContainer = containerMap.values.first()

        assertSame(firstContainer, secondContainer)
    }

    @Test
    @DisplayName("create_multiple_container_if_mcc_is_different[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-277152#]")
    fun create_multiple_container_if_mcc_is_different() {
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(
            DabBearer("mcc1", "ecc", "eid", "sid", "scids"),
            eTags,
            TestRadioStationDataCallback()
        )

        val mediaRequestObject2 = MediaRequestObject(
            DabBearer("mcc2", "ecc", "eid", "sid", "scids"),
            eTags,
            TestRadioStationDataCallback()
        )

        val containerMap = privateRequestContainer.get(requestController) as MutableMap<*, *>

        requestController.getRadioStationData(mediaRequestObject1)
        assertEquals(1, containerMap.size)

        requestController.getRadioStationData(mediaRequestObject2)
        assertEquals(2, containerMap.size)

        assertTrue(containerMap.keys.contains("mcc1"))
        assertTrue(containerMap.keys.contains("mcc2"))
    }

    @Test
    @DisplayName("container_with_mcc_of_null_are_supported[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-277152#]")
    fun container_with_mcc_of_null_are_supported() {
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(
            HdBearer("tx", "cc", "multicastId"),
            eTags,
            TestRadioStationDataCallback()
        )

        val containerMap = privateRequestContainer.get(requestController) as MutableMap<*, *>

        requestController.getRadioStationData(mediaRequestObject)
        assertEquals(1, containerMap.size)

        requestController.getRadioStationData(mediaRequestObject)
        assertEquals(1, containerMap.size)

        assertTrue(containerMap.keys.contains(null))
    }
}
