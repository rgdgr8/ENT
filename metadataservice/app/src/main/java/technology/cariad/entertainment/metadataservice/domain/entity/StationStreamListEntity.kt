package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for web streams of a station.
 *
 * @param streams already fetched web streams for the station, empty list if no stream is yet
 *                available.
 * @param etag a tag identifying the set of information, if available.
 */
data class StationStreamListEntity(
    val streams: List<StationStreamEntity> = listOf(),
    val etag: String?
)

