package technology.cariad.entertainment.metadataservice.common.position

import com.volkswagenag.nav.positioninformation.GeneralPositionInformationType
import technology.cariad.entertainment.metadataservice.common.util.Error
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PositionProviderTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val positionProviderApiMock = mockk<PositionProviderApi>()

    private val samplePosition = GeneralPositionInformationType().apply {
        latitude = 42.7
        longitude = 13.37
    }
    private val samplePositionResult = Success(
        PositionInformation(
            samplePosition.latitude, samplePosition.longitude
        )
    )

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun positionFlow_shouldEmitPositionOnSuccess() = runTest {
        every { positionProviderApiMock.positionFlow() } returns flowOf(samplePosition)

        val positionProvider =
            PositionProvider(positionProviderApiMock, testScope, testDispatcher)

        val position = positionProvider.positionFlow().first()

        Assert.assertEquals(samplePositionResult.result, (position as Success).result)
    }

    @Test
    fun positionFlow_shouldEmitErrorOnFailure() = runTest {
        every { positionProviderApiMock.positionFlow() } answers {
            flow { throw UnsupportedOperationException("") }
        }

        val positionProvider = PositionProvider(positionProviderApiMock, testScope, testDispatcher)

        val position = positionProvider.positionFlow().first()

        Assert.assertTrue(position is Error)
    }

    @Test
    fun lastPosition_shouldReturnMostRecentPosition() = runTest {
        every { positionProviderApiMock.positionFlow() } returns flowOf(null, samplePosition)

        val positionProvider = PositionProvider(positionProviderApiMock, testScope, testDispatcher)

        val position = positionProvider.lastPosition()

        Assert.assertEquals(samplePositionResult.result, (position as Success).result)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

