package de.esolutions.connectedradioclient.handlers.media

import android.content.Context
import android.net.Network
import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.api.NetworkDns
import de.esolutions.connectedradioclient.data.AccountManagerConfig
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.EndpointConfig
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.debug.DebugConfig
import de.esolutions.connectedradioclient.debug.DebugController
import de.esolutions.connectedradioclient.handlers.backend.controller.RequestController
import de.esolutions.connectedradioclient.handlers.backend.executor.ExecutorWorker
import de.esolutions.connectedradioclient.handlers.backend.executor.PausableThreadPoolExecutor
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.helper.ModifierAccessHacker
import de.esolutions.connectedradioclient.helper.TestController
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions
import de.esolutions.connectedradioclient.helper.TestRadioStationDataCallback
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ConRadClientTest {
    private lateinit var conRadClient: ConRadClient
    private lateinit var privateController: Field
    private val mockPausableThreadPoolExecutor = mock(PausableThreadPoolExecutor::class.java)

    @BeforeEach
    fun before() {
        conRadClient = ConRadClient(mock(Context::class.java), null)

        privateController = conRadClient::class.java.getDeclaredField("controller")
        privateController.isAccessible = true

        val privatePausableThreadPoolExecutor =
            ExecutorWorker::class.java.getDeclaredField("pausableThreadPoolExecutor")
        privatePausableThreadPoolExecutor.isAccessible = true

        val modifier = ModifierAccessHacker.modifier
        modifier.isAccessible = true
        modifier.setInt(
            privatePausableThreadPoolExecutor,
            privatePausableThreadPoolExecutor.modifiers and Modifier.FINAL.inv()
        )

        privatePausableThreadPoolExecutor.set(ExecutorWorker, mockPausableThreadPoolExecutor)
    }

    @Test
    fun it_should_be_always_the_correct_controller() {
        var controller = privateController.get(conRadClient)
        assertTrue(controller is RequestController)

        conRadClient.setDebugConfig(DebugConfig())
        controller = privateController.get(conRadClient)
        assertTrue(controller is DebugController)

        conRadClient.setDebugConfig(null)
        controller = privateController.get(conRadClient)
        assertTrue(controller is RequestController)
    }

    @Test
    fun add_to_container() {
        val latch = CountDownLatch(1)
        val testController = TestController(latch)
        privateController.set(conRadClient, testController)

        val fmBearer = FmBearer("mcc", "pi", "frequency")
        val testCallback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        conRadClient.getRadioStationData(fmBearer, eTags, testCallback)

        latch.await(10L, TimeUnit.SECONDS)
        val mediaRequestObject = testController.mediaRequestObject
        assertNotNull(mediaRequestObject)
        assertEquals(fmBearer, mediaRequestObject!!.bearer)
        assertEquals(eTags, mediaRequestObject.eTags)
        assertEquals(testCallback, mediaRequestObject.callback)
        assertEquals("mcc", mediaRequestObject.mcc)
    }

    @Test
    fun set_the_network_correct_and_start_queue() {
        TestHelpingFunctions.setHttpToDefault()

        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true
        val clientBefore = privateClient.get(Http) as OkHttpClient

        assertEquals(SocketFactory.getDefault(), clientBefore.socketFactory)
        //the default DNS is DNS.SYSTEM from the okhttp3, but it is not available in this class
        assertTrue(clientBefore.dns !is NetworkDns)

        val mockNetwork = mock(Network::class.java)
        val mockSocketFactory = mock(SocketFactory::class.java)
        Mockito.doAnswer { mockSocketFactory }.`when`(mockNetwork).socketFactory

        conRadClient.setNetwork(mockNetwork)

        val clientAfter = privateClient.get(Http) as OkHttpClient

        assertEquals(mockSocketFactory, clientAfter.socketFactory)
        assertTrue(clientAfter.dns is NetworkDns)
        verify(mockPausableThreadPoolExecutor).resume()

        TestHelpingFunctions.setHttpToDefault()
    }

    @Test
    fun stop_the_queue_on_null() {
        TestHelpingFunctions.setHttpToDefault()

        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true
        val clientBefore = privateClient.get(Http) as OkHttpClient

        assertEquals(SocketFactory.getDefault(), clientBefore.socketFactory)
        //the default DNS is DNS.SYSTEM from the okhttp3, but it is not available in this class
        assertTrue(clientBefore.dns !is NetworkDns)

        conRadClient.setNetwork(null)

        val clientAfter = privateClient.get(Http) as OkHttpClient

        assertEquals(SocketFactory.getDefault(), clientAfter.socketFactory)
        //the default DNS is DNS.SYSTEM from the okhttp3, but it is not available in this class
        assertTrue(clientAfter.dns !is NetworkDns)
        verify(mockPausableThreadPoolExecutor).pause()
    }

    @Test
    fun return_the_saved_token() {
        val tokenHandler = TokenHandler(mock(Context::class.java))
        conRadClient.setToken("token123")
        val tokenFromHandler = tokenHandler.getToken("operationId")

        assertEquals("token123", tokenFromHandler)

        conRadClient.setToken(null)
        val nullTokenFromHandler = tokenHandler.getToken("operationId")

        //return null because in the test situation is no AccountManager available
        assertNull(nullTokenFromHandler)
    }

    @Test
    @DisplayName("set_the_given_config_values[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-256035#]")
    fun set_the_given_config_objects() {
        val accountManagerConfig = AccountManagerConfig(
            "serviceId",
            "accountType",
            "accountName",
            "optionServiceId",
            "optionOperationId"
        )
        val endpointConfig = EndpointConfig("url", "operationId")

        conRadClient.setConfig(accountManagerConfig, endpointConfig)

        assertSame(accountManagerConfig, TokenHandler.accountManagerConfig)
        assertSame(endpointConfig, Config.endpointConfig)
    }
}