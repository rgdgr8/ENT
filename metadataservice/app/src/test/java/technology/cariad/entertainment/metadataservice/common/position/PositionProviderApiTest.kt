package technology.cariad.entertainment.metadataservice.common.position

import android.os.IBinder
import com.volkswagenag.nav.positioninformation.GeneralPositionInformationType
import com.volkswagenag.nav.positioninformation.IPositionInformation
import technology.cariad.entertainment.metadataservice.common.serviceconnection.ServiceConnection
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.Called
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PositionProviderApiTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val serviceConnectionMock = mockk<ServiceConnection>()
    private val binderMock = mockk<IBinder>()
    private val serviceMock = mockk<IPositionInformation>()
    private val positionInformationCallbackMock = mockk<PositionInformationCallback>()

    private val positionProviderApi = PositionProviderApi(serviceConnectionMock, 500)

    private val channelSlot = slot<ProducerScope<GeneralPositionInformationType?>>()

    private val samplePosition = GeneralPositionInformationType().apply {
        latitude = 42.7
        longitude = 13.37
    }

    @Before
    fun setup() {
        LoggerMock()

        mockkObject(PositionInformationCallback)
        every { PositionInformationCallback.create(capture(channelSlot)) } returns
                positionInformationCallbackMock

        mockkStatic(IPositionInformation.Stub::class)
        every { IPositionInformation.Stub.asInterface(any()) } returns serviceMock

        every { serviceMock.registerPositionInformation(any(), any()) } just Runs
        every { serviceMock.unRegisterPositionInformation(any()) } just Runs
    }

    @Test
    fun positionFlow_shouldEmitPositions() = testScope.runBlockingTest {
        every { serviceConnectionMock.serviceConnectionFlow() } returns
                flowOf(binderMock)

        val positions = mutableListOf<GeneralPositionInformationType?>()

        val job = testScope.launch {
            positionProviderApi.positionFlow().toList(positions)
        }

        channelSlot.captured.trySend(samplePosition)
        channelSlot.captured.trySend(null)

        Assert.assertEquals(positions, listOf(samplePosition, null))

        job.cancel()
    }

    @Test
    fun positionFlow_shouldRegisterForPositionCallbackWhenServiceConnects() =
        testScope.runBlockingTest {
            every { serviceConnectionMock.serviceConnectionFlow() } returns
                    flowOf(binderMock)

            val job = positionProviderApi.positionFlow().launchIn(testScope)

            verify {
                serviceMock.registerPositionInformation(500, positionInformationCallbackMock)
            }

            job.cancel()
        }

    @Test
    fun positionFlow_shouldUnregisterPositionCallbackWhenFlowCollectionFinishes() =
        testScope.runBlockingTest {
            every { serviceConnectionMock.serviceConnectionFlow() } returns
                    flowOf(binderMock)

            val job = testScope.launch {
                positionProviderApi.positionFlow().firstOrNull()
            }
            channelSlot.captured.trySend(null)

            verify {
                serviceMock.unRegisterPositionInformation(positionInformationCallbackMock)
            }

            job.join()
        }

    @Test
    fun positionFlow_shouldDoNothingUntilServiceAvailable() =
        testScope.runBlockingTest {
            every { IPositionInformation.Stub.asInterface(null) } returns null
            every { serviceConnectionMock.serviceConnectionFlow() } returns
                    flowOf(null)

            val job = positionProviderApi.positionFlow().launchIn(testScope)

            verify {
                serviceMock wasNot Called
            }

            job.cancel()
        }


    @After
    fun tearDown() {
        unmockkAll()
    }

}

