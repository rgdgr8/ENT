package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecord

import android.media.AudioFormat
import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder.RecordingConfig
import org.junit.Assert
import org.junit.Test

class AudioConfigConverterTest {

    @Test
    fun convertToAudioFormatChannel_shouldReturnChannelMask() {
        val mono = convertToAudioFormatChannel(RecordingConfig.Channel.IN_MONO)
        Assert.assertEquals(AudioFormat.CHANNEL_IN_MONO, mono)

        val stereo = convertToAudioFormatChannel(RecordingConfig.Channel.IN_STEREO)
        Assert.assertEquals(AudioFormat.CHANNEL_IN_STEREO, stereo)
    }

    @Test
    fun convertToAudioFormatEncoding_shouldReturnEncodingConstant() {
        val pcm8bit = convertToAudioFormatEncoding(RecordingConfig.Encoding.PCM_8BIT)
        Assert.assertEquals(AudioFormat.ENCODING_PCM_8BIT, pcm8bit)

        val pcm16bit = convertToAudioFormatEncoding(RecordingConfig.Encoding.PCM_16BIT)
        Assert.assertEquals(AudioFormat.ENCODING_PCM_16BIT, pcm16bit)
    }
}

