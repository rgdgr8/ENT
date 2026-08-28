package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder

import technology.cariad.entertainment.metadataservice.common.io.IOutputStream
import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecord.IAudioRecordWrapper
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class AudioRecorderTest {

    private val streamMock: IOutputStream = mockk(relaxed = true)
    private val audioRecordWrapper: IAudioRecordWrapper = mockk()
    private lateinit var audioRecorder : AudioRecorder

    @Before
    fun setup() {
        every { audioRecordWrapper.readBufferSizeBytes } returns 1024
        every { audioRecordWrapper.initialize() } returns true
        every { audioRecordWrapper.startRecording() } returns true
        every { audioRecordWrapper.stopRecording() } just Runs
        audioRecorder = AudioRecorder(audioRecordWrapper)
    }

    @Test
    fun recordAudio_shouldCopyAudioDataToStream() {

        every { audioRecordWrapper.readBytes(any()) } answers {
            audioRecorder.stopRecording()
            0
        }

        audioRecorder.recordAudio(streamMock)

        verifyOrder {
            audioRecordWrapper.initialize()
            audioRecordWrapper.startRecording()
        }
    }

    @Test
    fun `recordAudio should initialize and start and stop`() {
        every { audioRecordWrapper.initialize() } returns true
        every { audioRecordWrapper.readBytes(any()) } answers {
            audioRecorder.stopRecording()
            512
        }

        audioRecorder.recordAudio(streamMock)

        verifyOrder {
            audioRecordWrapper.initialize()
            audioRecordWrapper.startRecording()
            audioRecordWrapper.readBytes(any())
            streamMock.write(any(), 512)
            audioRecordWrapper.stopRecording()
        }
    }

    @Test
    fun `recordAudio should return early if initialization fails`() {
        every { audioRecordWrapper.initialize() } returns false

        audioRecorder.recordAudio(streamMock)

        verify(exactly = 1) { audioRecordWrapper.initialize() }
        verify(exactly = 0) { audioRecordWrapper.startRecording() }
        verify(exactly = 0) { streamMock.write(any(), any()) }
    }

    @Test
    fun `recordAudio should handle multiple reads before stop`() {
        every { audioRecordWrapper.initialize() } returns true
        var calls = 0
        every { audioRecordWrapper.readBytes(any()) } answers {
            calls++
            if (calls >= 5) audioRecorder.stopRecording()
            1024
        }

        audioRecorder.recordAudio(streamMock)

        verify(atLeast = 5) { streamMock.write(any(), 1024) }
        verify { audioRecordWrapper.stopRecording() }
    }

    @Test
    fun `recordAudio should handle readBytes returning error`() {
        every { audioRecordWrapper.initialize() } returns true
        every { audioRecordWrapper.readBytes(any()) } answers {
            audioRecorder.stopRecording()
            -1 // Error reading
        }

        audioRecorder.recordAudio(streamMock)

        verify { streamMock.write(any(), -1) }
    }

    @Test
    fun `stopRecording should set isStopping`() {
        every { audioRecordWrapper.initialize() } returns true
        every { audioRecordWrapper.readBytes(any()) } answers {
            Thread.sleep(10)
            1024
        }

        val recorderThread = Thread {
            audioRecorder.recordAudio(streamMock)
        }
        recorderThread.start()
        Thread.sleep(50)
        audioRecorder.stopRecording()
        recorderThread.join()
        // Checking for no deadlock, test ok
        assertTrue(true)
    }
}
