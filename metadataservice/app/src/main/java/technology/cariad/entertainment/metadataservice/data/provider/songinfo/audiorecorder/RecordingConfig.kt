package technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder

data class RecordingConfig(
    val inputDeviceAddress: String,
    val sampleRate: Int,
    val channels: Channel,
    val encoding: Encoding
) {
    enum class Channel(val channelCount: Int) {
        IN_MONO(1),
        IN_STEREO(2)
    }

    enum class Encoding(val sampleSize: Int) {
        PCM_8BIT(8),
        PCM_16BIT(16)
    }
}

