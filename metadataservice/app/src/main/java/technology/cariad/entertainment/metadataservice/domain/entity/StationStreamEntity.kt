package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for a web based audio stream for a radio station.
 *
 * @param url the url that can be used to obtain the streams binary data.
 * @param mimeType the type of the station stream if available, e.g. 'audio/mp3'.
 * @param bitrateKbps the bitrate of the station stream in kilobytes per second if available.
 * @param offsetMillis offset of the streams audio compared to live broadcast audio in milliseconds
 *                     if available.
 * @param quality the flag to indicate the quality of the stream (HIGH/LOW)
 */

data class StationStreamEntity(
    val url: String,
    val offsetMillis: Int? = null,
    val quality: QualityType,
) {
    companion object {
        enum class QualityType(val value: Int) {
            UNKNOWN(-1),
            HIGH(1),
            LOW(2)
        }
    }
}
