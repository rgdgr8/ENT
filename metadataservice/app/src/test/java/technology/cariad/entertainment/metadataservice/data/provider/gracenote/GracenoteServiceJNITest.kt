package technology.cariad.entertainment.metadataservice.data.provider.gracenote

import de.esolutions.gracenoteservice.gntest.GraceNoteServiceJNI
import technology.cariad.entertainment.metadataservice.common.util.loadLibrary
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

class GracenoteServiceJNITest {

    private val gracenoteServiceCallbackMock = spyk<IGracenoteServiceCallback>()

    private lateinit var gracenoteService: GraceNoteServiceJNI

    private val sampleMetadata = arrayOf("some metadata")
    private val sampleImage = "some image".toByteArray()
    private val sampleStation = RadioStationInfo()

    @Before
    fun setup() {
        mockkStatic(::loadLibrary)
        every { loadLibrary(any()) } returns Unit
        gracenoteService = GraceNoteServiceJNI(gracenoteServiceCallbackMock)
    }

    @After
    fun tearDown() {
        unmockkStatic(System::class)
    }

    @Test
    fun onRecognitionDataReceived_shouldCallBack() {
        gracenoteService.onRecognitionDataReceived(sampleMetadata, sampleImage)

        verify(exactly = 1) {
            gracenoteServiceCallbackMock.onRecognitionResult(sampleMetadata, sampleImage)
        }

        confirmVerified(gracenoteServiceCallbackMock)
    }

    @Test
    fun onRecognitionClearResult_shouldCallBack() {
        gracenoteService.onRecognitionClearResult()

        verify(exactly = 1) {
            gracenoteServiceCallbackMock.onRecognitionClearResult()
        }

        confirmVerified(gracenoteServiceCallbackMock)
    }

    @Test
    fun onRadioStationResult_shouldCallBack() {
        gracenoteService.onRadioStationDataReceived(sampleStation, sampleImage)

        verify(exactly = 1) {
            gracenoteServiceCallbackMock.onRadioStationResult(
                sampleStation,
                sampleImage
            )
        }

        confirmVerified(gracenoteServiceCallbackMock)
    }
}

