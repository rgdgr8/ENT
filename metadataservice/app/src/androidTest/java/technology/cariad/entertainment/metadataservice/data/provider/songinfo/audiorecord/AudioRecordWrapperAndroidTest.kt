package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecord

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder.RecordingConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioRecordWrapperInstrumentedTest {

    private lateinit var context: Context
    private lateinit var recordingConfig: RecordingConfig

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        recordingConfig = RecordingConfig(
            sampleRate = 44100,
            encoding = RecordingConfig.Encoding.PCM_16BIT,
            channels = RecordingConfig.Channel.IN_STEREO,
            inputDeviceAddress = ""
        )
    }

    @Test
    fun testInitializeAndStartStopRecording() {
        val wrapper = AudioRecordWrapper(context, recordingConfig)

        val started = wrapper.startRecording()
        assertTrue(started)

        val buffer = ByteArray(wrapper.readBufferSizeBytes)
        val bytesRead = wrapper.readBytes(buffer)
        assertTrue(bytesRead >= 0)

        wrapper.stopRecording()
    }

    @Test
    fun testReadBytesReturnsMinusOneWhenStopped() {
        val wrapper = AudioRecordWrapper(context, recordingConfig)
        wrapper.initialize()

        val buffer = ByteArray(wrapper.readBufferSizeBytes)
        val result = wrapper.readBytes(buffer)

        assertTrue(result <= 0)
    }

    @Test
    fun testDoubleStartRecordingDoesNotCrash() {
        val wrapper = AudioRecordWrapper(context, recordingConfig)
        wrapper.initialize()
        wrapper.startRecording()
        val secondStart = wrapper.startRecording()

        assertTrue(secondStart)
        wrapper.stopRecording()
    }

    @Test
    fun testStopRecordingWithoutStartDoesNotCrash() {
        val wrapper = AudioRecordWrapper(context, recordingConfig)
        wrapper.initialize()
        try {
            wrapper.stopRecording()
        } catch (e: Exception) {
            fail("stopRecording() because no startRecording")
        }
    }

    @Test
    fun testInitializeFailsWithInvalidDeviceAddress() {
        val config = recordingConfig.copy(inputDeviceAddress = "non-existent-device")
        val wrapper = AudioRecordWrapper(context, config)
        val result = wrapper.initialize()
        assertFalse(result)
    }
}

