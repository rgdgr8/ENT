package technology.cariad.entertainment.metadataservice.common.servicelist

import de.esolutions.coreservices.csm.service.systemservice.ServiceListEntry
import technology.cariad.entertainment.metadataservice.data.provider.ConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.After
import org.junit.Assert
import org.junit.Test

@ExperimentalCoroutinesApi
class ServiceListClientTest {
    init {
        LoggerMock()
    }

    private val serviceListApiClientMock: ServiceListApiClient = mockk(relaxed = true)

    private val serviceListEntryGracenoteEnabled = ServiceListEntry().apply {
        serviceID = ServiceIdentifier.GRACENOTE_V1.serviceName
        enableDisableServiceState = true
        httpOperations = arrayOf()
    }

    private val serviceListEntryGracenoteDisabled = ServiceListEntry().apply {
        serviceID = ServiceIdentifier.GRACENOTE_V1.serviceName
        enableDisableServiceState = false
        httpOperations = arrayOf()
    }

    private val serviceListEntryHybridRadioDataEnabled = ServiceListEntry().apply {
        serviceID = ServiceIdentifier.CONNECTEDRADIO_V2.serviceName
        enableDisableServiceState = true
        httpOperations = arrayOf()
    }

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = CoroutineScope(testDispatcher)
    private val configurationProvider = ConfigurationProvider()
    private val serviceListClient by lazy {
        ServiceListClient(
            serviceListApiClientMock,
            configurationProvider,
            testDispatcher
        )
    }

    @Test
    fun serviceEnabled_returnsTrueWhenServiceIdEnabled() {
        every { serviceListApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf(serviceListEntryGracenoteEnabled))
            awaitClose()
        }

        serviceListClient.serviceListFlow().launchIn(testScope)

        val enabled = serviceListClient.serviceEnabled(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertTrue(enabled)
    }

    @Test
    fun serviceEnabled_returnsFalseWhenServiceListEmpty() {
        every { serviceListApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf())
            awaitClose()
        }

        serviceListClient.serviceListFlow().launchIn(testScope)

        val enabled = serviceListClient.serviceEnabled(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertFalse(enabled)
    }

    @Test
    fun serviceEnabled_returnsFalseWhenServiceListEntryMissing() {
        every { serviceListApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf(serviceListEntryHybridRadioDataEnabled))
            awaitClose()
        }

        serviceListClient.serviceListFlow().launchIn(testScope)

        val enabled = serviceListClient.serviceEnabled(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertFalse(enabled)
    }

    @Test
    fun serviceEnabled_returnsFalseWhenServiceIdDisabled() {
        every { serviceListApiClientMock.asFlow() } returns channelFlow {
            trySend(listOf(serviceListEntryGracenoteDisabled))
            awaitClose()
        }

        serviceListClient.serviceListFlow().launchIn(testScope)

        val enabled = serviceListClient.serviceEnabled(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertFalse(enabled)
    }

    @Test
    fun serviceEnabled_returnsFalseAfterUnhandledException() {
        every { serviceListApiClientMock.asFlow() } returns flow {
            emit(listOf(serviceListEntryGracenoteEnabled))
            throw NullPointerException()
        }

        serviceListClient.serviceListFlow().launchIn(testScope)

        val enabled = serviceListClient.serviceEnabled(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertFalse(enabled)
    }

    @Test
    fun serviceEnabled_returnsFalseWhenServiceNotStarted() {
        val enabled = serviceListClient.serviceEnabled(ServiceIdentifier.GRACENOTE_V1)

        Assert.assertFalse(enabled)
    }

    @After
    fun tearDown() {
        try {
            testScope.cancel()
        } catch (e: Exception) {
        }
        unmockkAll()
    }
}

