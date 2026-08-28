package technology.cariad.entertainment.metadataservice.common.network

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class backTest {

    init {
        LoggerMock()
    }

    private val networkMock = mockk<Network>()
    private val networkCapabilitiesMock = mockk<NetworkCapabilities>()
    private val linkPropertiesWithInterfaceMock = mockk<LinkProperties>()
    private val linkPropertiesWithoutInterfaceMock = mockk<LinkProperties>()
    private val connectivityManagerMock = mockk<ConnectivityManager>(relaxed = true)


    @Before
    fun setup() {
        every { linkPropertiesWithInterfaceMock.interfaceName } returns "eth42"
        every { linkPropertiesWithoutInterfaceMock.interfaceName } returns null
    }

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Test
    fun onAvailable_shouldEmitNothing() =
        testScope.runBlockingTest {
            val flow = channelFlow {
                val networkCallback = NetworkCallback.create(
                    Apn.APN_VENDOR,
                    connectivityManagerMock,
                    this
                )

                networkCallback.onAvailable(networkMock)
            }

            val networks = flow.toList()
            Assert.assertTrue(networks.isEmpty())
        }

    @Test
    fun onCapabilitiesChanged_shouldEmitNothing() =
        testScope.runBlockingTest {
            val flow = channelFlow {
                val networkCallback = NetworkCallback.create(
                    Apn.APN_VENDOR,
                    connectivityManagerMock,
                    this
                )

                networkCallback.onCapabilitiesChanged(networkMock, networkCapabilitiesMock)
            }

            val networks = flow.toList()
            Assert.assertTrue(networks.isEmpty())
        }

    @Test
    fun onLinkPropertiesChanged_shouldEmiWhenInterfaceAvailable() =
        testScope.runBlockingTest {
            val flow = channelFlow {
                val networkCallback = NetworkCallback.create(
                    Apn.APN_VENDOR,
                    connectivityManagerMock,
                    this
                )

                networkCallback.onLinkPropertiesChanged(
                    networkMock,
                    linkPropertiesWithInterfaceMock
                )
            }

            val network = flow.first()
            Assert.assertNotNull(network)
        }

    @Test
    fun onLinkPropertiesChanged_shouldNotEmitWheInterfaceNotAvailable() =
        testScope.runBlockingTest {
            val flow = channelFlow {
                val networkCallback = NetworkCallback.create(
                    Apn.APN_VENDOR,
                    connectivityManagerMock,
                    this
                )

                networkCallback.onLinkPropertiesChanged(
                    networkMock,
                    linkPropertiesWithoutInterfaceMock
                )
            }

            val networks = flow.toList()
            Assert.assertTrue(networks.isEmpty())
        }

    @Test
    fun onBlockedStatusChanged_shouldEmitWhenUnblockedAndInterfaceAvailable() =
        testScope.runBlockingTest {
            val flow = channelFlow {
                val networkCallback = NetworkCallback.create(
                    Apn.APN_VENDOR,
                    connectivityManagerMock,
                    this
                )

                networkCallback.onLinkPropertiesChanged(
                    networkMock,
                    linkPropertiesWithInterfaceMock
                )
                networkCallback.onBlockedStatusChanged(networkMock, false)
            }

            val network = flow.first()
            Assert.assertNotNull(network)
        }

    @Test
    fun onBlockedStatusChanged_shouldNotEmitWhenUnblockedAndInterfaceNotAvailable() =
        testScope.runBlockingTest {
            val flow = channelFlow {
                val networkCallback = NetworkCallback.create(
                    Apn.APN_VENDOR,
                    connectivityManagerMock,
                    this
                )

                networkCallback.onBlockedStatusChanged(networkMock, false)
            }

            val networks = flow.toList()
            Assert.assertTrue(networks.isEmpty())
        }

    @Test
    fun onBlockedStatusChanged_shouldEmitNullWhenBlocked() = testScope.runBlockingTest {
        val flow = channelFlow {
            val networkCallback = NetworkCallback.create(Apn.APN_VENDOR, connectivityManagerMock, this)

            networkCallback.onBlockedStatusChanged(networkMock, true)
        }

        val network = flow.first()
        Assert.assertNull(network)
    }

    @Test
    fun networkCallback_shouldEmitNullWhenNetworkLost() = testScope.runBlockingTest {
        val flow = channelFlow {
            val networkCallback = NetworkCallback.create(Apn.APN_VENDOR, connectivityManagerMock, this)

            networkCallback.onLost(networkMock)
        }

        val network = flow.first()
        Assert.assertNull(network)
    }

    @Test(expected = NetworkUnavailableException::class)
    fun networkCallback_shouldThrowWhenNetworkUnavailable() =
        testScope.runBlockingTest {
            val flow = channelFlow {
                val networkCallback = NetworkCallback.create(
                    Apn.APN_VENDOR,
                    connectivityManagerMock,
                    this
                )

                networkCallback.onUnavailable()
            }

            flow.collect()
        }
}

