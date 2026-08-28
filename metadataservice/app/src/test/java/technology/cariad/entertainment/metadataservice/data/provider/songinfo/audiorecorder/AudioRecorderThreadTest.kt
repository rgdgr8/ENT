package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder

import technology.cariad.entertainment.metadataservice.common.io.IOutputStream
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.atomic.AtomicLong

class AudioRecorderThreadTest {

    private val audioRecorderMock = mockk<IAudioRecorder>()
    private val streamMock = mockk<IOutputStream>()
    private val audioRecorderThread = AudioRecorderThread(audioRecorderMock)

    @Test
    fun start_shouldRecordAudioInSeparateThread() {
        val recordingThreadId = AtomicLong(-1)

        every { audioRecorderMock.recordAudio(any()) } answers {
            recordingThreadId.set(Thread.currentThread().id)
        }
        every { audioRecorderMock.stopRecording() } just Runs

        audioRecorderThread.start(streamMock)
        audioRecorderThread.stop()

        Assert.assertNotEquals(-1, recordingThreadId.get())
        Assert.assertNotEquals(Thread.currentThread().id, recordingThreadId.get())

        excludeRecords { audioRecorderMock.stopRecording() }
        verify(exactly = 1) { audioRecorderMock.recordAudio(any()) }
        confirmVerified(audioRecorderMock)
    }

    @Test
    fun start_shouldDoNothingIfAlreadyStarted() {
        every { audioRecorderMock.recordAudio(any()) } just Runs
        every { audioRecorderMock.stopRecording() } just Runs

        audioRecorderThread.start(streamMock)
        audioRecorderThread.start(streamMock)
        audioRecorderThread.stop()

        excludeRecords { audioRecorderMock.stopRecording() }
        verify(exactly = 1) { audioRecorderMock.recordAudio(any()) }
        confirmVerified(audioRecorderMock)
    }

    @Test
    fun stop_shouldStopAudioRecording() {
        every { audioRecorderMock.recordAudio(any()) } just Runs
        every { audioRecorderMock.stopRecording() } just Runs

        audioRecorderThread.start(streamMock)
        audioRecorderThread.stop()

        excludeRecords { audioRecorderMock.recordAudio(any()) }
        verify(exactly = 1) { audioRecorderMock.stopRecording() }
        confirmVerified(audioRecorderMock)
    }

    @Test
    fun stop_shouldDoNothingIfAlreadyStopped() {
        every { audioRecorderMock.recordAudio(any()) } just Runs
        every { audioRecorderMock.stopRecording() } just Runs

        audioRecorderThread.start(streamMock)
        audioRecorderThread.stop()
        audioRecorderThread.stop()

        excludeRecords { audioRecorderMock.recordAudio(any()) }
        verify(exactly = 1) { audioRecorderMock.stopRecording() }
        confirmVerified(audioRecorderMock)
    }
}

