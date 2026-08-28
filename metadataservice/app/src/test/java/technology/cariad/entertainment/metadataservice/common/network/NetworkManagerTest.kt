package technology.cariad.entertainment.metadataservice.common.network

import android.net.ConnectivityManager
import android.net.NetworkRequest
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NetworkManagerTest {

    init {
        LoggerMock()
    }

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val connectivityManagerMock = mockk<ConnectivityManager>(relaxed = true)
    private val networkRequestMock = mockk<NetworkRequest>()
    private val networkCallbackMock = mockk<NetworkCallback>()

    private val networkManager = NetworkManager(connectivityManagerMock, testDispatcher)
    private val transportTypes = listOf<Int>()
    private val capabilities = listOf<Int>()

    private val channelSlot = slot<ProducerScope<NetworkInfo?>>()

    @Before
    fun setup() {
        mockkObject(NetworkCallback)
        every { NetworkCallback.create(any(), connectivityManagerMock, capture(channelSlot)) } returns networkCallbackMock

        mockkStatic(::getTransportTypes)
        every { getTransportTypes(any(), any()) } returns transportTypes

        mockkStatic(::getNetworkCapabilities)
        every { getNetworkCapabilities(any()) } returns capabilities

        mockkStatic(::buildNetworkRequest)
        every { buildNetworkRequest(any(), any()) } returns networkRequestMock

        every {
            connectivityManagerMock.registerDefaultNetworkCallback(networkCallbackMock)
        } just Runs
        every { connectivityManagerMock.unregisterNetworkCallback(networkCallbackMock) } just Runs
    }

    @Test
    fun constructor_shouldTakeOptionalDispatcher() {
        NetworkManager(connectivityManagerMock)
        NetworkManager(connectivityManagerMock, testDispatcher)
    }

    @Test
    fun getNetworkInfoFlow_shouldCacheFlowForApnAndTethering() {
        val customerNoTethering = networkManager.getNetworkInfoFlow(Apn.APN_CUSTOMER, false)
        val customerNoTethering2 = networkManager.getNetworkInfoFlow(Apn.APN_CUSTOMER, false)

        val customerTethering = networkManager.getNetworkInfoFlow(Apn.APN_CUSTOMER, true)
        val vendorNoTethering = networkManager.getNetworkInfoFlow(Apn.APN_VENDOR, false)

        Assert.assertEquals(customerNoTethering, customerNoTethering2)
        Assert.assertNotEquals(customerNoTethering, customerTethering)
        Assert.assertNotEquals(customerNoTethering, vendorNoTethering)
    }

    @Test
    fun getNetworkInfoFlow_shouldResetNetworkOnCompletion() = testScope.runBlockingTest {
        val networkInfos = mutableListOf<NetworkInfo?>()

        val job = launch {
            networkManager.getNetworkInfoFlow(Apn.APN_CUSTOMER, false).toList(networkInfos)
        }

        Assert.assertTrue(networkInfos.isEmpty())

        channelSlot.captured.close()

        Assert.assertEquals(1, networkInfos.size)
        Assert.assertEquals(null, networkInfos.first())

        verify {
            connectivityManagerMock.registerDefaultNetworkCallback(networkCallbackMock)
            connectivityManagerMock.unregisterNetworkCallback(networkCallbackMock)
        }

        confirmVerified(connectivityManagerMock)

        job.cancel()
    }


    @After
    fun tearDown() {
        unmockkAll()
    }
}

