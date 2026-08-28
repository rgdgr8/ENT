package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for textual metadata of a radio station.
 *
 * @param shortName the short name for the station, if available.
 * @param mediumName the medium name for the station, if available.
 * @param longName the longName name for the station, if available.
 * @param description the description text for the station, if available.
 * @param genres a list of genres ordered by relevance, if available.
 * @param etag a tag identifying the set of information, if available.
 */
data class StationMetadataEntity(
    val shortName: String? = null,
    val mediumName: String? = null,
    val longName: String? = null,
    val description: String? = null,
    val genres: List<String>?,
    var etag: String?
)
