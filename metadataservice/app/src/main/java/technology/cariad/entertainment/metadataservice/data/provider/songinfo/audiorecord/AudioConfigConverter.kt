package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecord

import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder.RecordingConfig

internal fun convertToAudioFormatChannel(channel: RecordingConfig.Channel): Int =
    when (channel) {
        RecordingConfig.Channel.IN_MONO -> android.media.AudioFormat.CHANNEL_IN_MONO
        RecordingConfig.Channel.IN_STEREO -> android.media.AudioFormat.CHANNEL_IN_STEREO
    }


internal fun convertToAudioFormatEncoding(encoding: RecordingConfig.Encoding): Int =
    when (encoding) {
        RecordingConfig.Encoding.PCM_8BIT -> android.media.AudioFormat.ENCODING_PCM_8BIT
        RecordingConfig.Encoding.PCM_16BIT -> android.media.AudioFormat.ENCODING_PCM_16BIT
    }

