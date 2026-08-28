package technology.cariad.entertainment.metadataservice.common.disablereasons

import de.esolutions.coreservices.csm.service.systemservice.DisableReasons
import de.esolutions.coreservices.csm.service.systemservice.ReasonBackend
import technology.cariad.entertainment.metadataservice.data.provider.ConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.android.LogMock
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class DisableReasonsClientTest {
    init {
        LogMock()
    }

    private val disableReasonsApiClientMock: DisableReasonsApiClient = mockk(relaxed = true)

    private val disableReasonsGracenoteNone = DisableReasons().apply {
        serviceId = ServiceIdentifier.GRACENOTE_V1.serviceName
        reasonBackend = intArrayOf()
        reasonConfiguration = intArrayOf()
        reasonConnectivity = intArrayOf()
        reasonLicense = intArrayOf()
    }

    private val disableReasonsGracenoteDisabled = DisableReasons().apply {
        serviceId = ServiceIdentifier.GRACENOTE_V1.serviceName
        reasonBackend = intArrayOf(ReasonBackend.REASONBACKEND_NOT_CONFIGURED)
        reasonConfiguration = intArrayOf()
        reasonConnectivity = intArrayOf()
        reasonLicense = intArrayOf()
    }

    private val disableReasonsHybridRadioDataEnabled = DisableReasons().apply {
        serviceId = ServiceIdentifier.CONNECTEDRADIO_V2.serviceName
    }

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = CoroutineScope(testDispatcher)
    private val configurationProvider = ConfigurationProvider()
    private val disableReasonsClient by lazy {
        DisableReasonsClient(
            disableReasonsApiClientMock,
            testDispatcher,
            configurationProvider
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun hasDisableReasons_returnsFalseWhenAllDisableReasonsEmpty() = runTest {
        every { disableReasonsApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf(disableReasonsGracenoteNone))
            awaitClose()
        }

        disableReasonsClient.fakeDisabledReasons(false)

        val disabled = disableReasonsClient.hasDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertFalse(disabled)
    }

    @Test
    fun hasDisableReasons_returnsTrueWhenDisableReasonEmpty() {
        every { disableReasonsApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf())
            awaitClose()
        }

        disableReasonsClient.disableReasonsFlow().launchIn(testScope)

        val disabled = disableReasonsClient.hasDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertTrue(disabled)
    }

    @Test
    fun hasDisableReasons_returnsTrueWhenDisableReasonsMissing() {
        every { disableReasonsApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf(disableReasonsHybridRadioDataEnabled))
            awaitClose()
        }

        disableReasonsClient.disableReasonsFlow().launchIn(testScope)

        val disabled = disableReasonsClient.hasDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertTrue(disabled)
    }

    @Test
    fun hasDisableReasons_returnsTrueWhenAnyDisableReasonsPresent() {
        every { disableReasonsApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf(disableReasonsGracenoteDisabled))
            awaitClose()
        }

        disableReasonsClient.disableReasonsFlow().launchIn(testScope)

        val disabled = disableReasonsClient.hasDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertTrue(disabled)
    }

    @Test
    fun hasDisableReasons_returnsFalseAfterUnhandledException() {
        every { disableReasonsApiClientMock.asFlow() } returns flow {
            emit(listOf(disableReasonsGracenoteNone))
            throw NullPointerException()
        }

        disableReasonsClient.disableReasonsFlow().launchIn(testScope)

        val disabled = disableReasonsClient.hasDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertTrue(disabled)
    }


    @Test
    fun hasDisableReasons_returnsTrueWhenServiceNotStarted() {
        val disabled = disableReasonsClient.hasDisableReasons(ServiceIdentifier.GRACENOTE_V1)

        Assert.assertTrue(disabled)
    }

    @Test
    fun hasAnyDisableReasons_returnsTrueIfAnyDisableReasonsPresent() = runTest {
        every { disableReasonsApiClientMock.hasAnyDisableReasons(any()) } returns true

        val disabled = disableReasonsClient.hasAnyDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertTrue(disabled)
    }

    @Test
    fun hasAnyDisableReasons_returnsFalseIfNoDisableReasonsPresent() = runTest {
        every { disableReasonsApiClientMock.hasAnyDisableReasons(any()) } returns false

        val disabled = disableReasonsClient.hasAnyDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertFalse(disabled)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

