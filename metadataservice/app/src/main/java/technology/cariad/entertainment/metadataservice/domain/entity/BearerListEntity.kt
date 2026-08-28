package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for bearers of a station.
 *
 * @param bearers list of bearers that all identify the station.
 * @param etag a tag identifying the set of information, if available.
 */
data class BearerListEntity(
    var bearers: List<IBearerEntity> = listOf(),
    var etag: String?
)

