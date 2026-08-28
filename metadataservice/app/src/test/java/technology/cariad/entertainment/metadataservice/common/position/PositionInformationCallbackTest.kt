package technology.cariad.entertainment.metadataservice.common.position

import android.os.Binder
import com.volkswagenag.nav.positioninformation.GeneralPositionInformationType
import com.volkswagenag.nav.positioninformation.IPositionInformationCallback
import com.volkswagenag.nav.positioninformation.RoadInformationType
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PositionInformationCallbackTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val testPosition = GeneralPositionInformationType()
    private val testRoadInformation = RoadInformationType()

    @Before
    fun setup() {
        LoggerMock()

        mockkConstructor(Binder::class)
        mockkConstructor(IPositionInformationCallback.Stub::class)
        mockkConstructor(PositionInformationCallback::class)

        every {
            anyConstructed<PositionInformationCallback>().attachInterface(any(), any())
        } just Runs
    }

    @Test
    fun onGeneralPositionInformation_shouldEmitPosition() = testScope.runBlockingTest {
        val flow = channelFlow<GeneralPositionInformationType?> {
            val positionInformationCallback = PositionInformationCallback.create(this)
            positionInformationCallback.onGeneralPositionInformation(testPosition)
        }

        val binders = flow.toList()

        Assert.assertEquals(listOf(testPosition), binders)
    }

    @Test
    fun onRoadInformation_shouldEmitNothing() = testScope.runBlockingTest {
        val flow = channelFlow<GeneralPositionInformationType?> {
            val positionInformationCallback = PositionInformationCallback.create(this)
            positionInformationCallback.onRoadInformation(true, testRoadInformation)
        }

        val binders = flow.toList()

        Assert.assertEquals(listOf<RoadInformationType>(), binders)
    }

    @Test
    fun onError_shouldEmitNull() = testScope.runBlockingTest {
        val flow = channelFlow<GeneralPositionInformationType?> {
            val positionInformationCallback = PositionInformationCallback.create(this)
            positionInformationCallback.onError(0)
        }

        val binders = flow.toList()

        Assert.assertEquals(listOf(null), binders)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

