package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Cover art data to request a cover art image from GN service.
 *
 * @param artist (V1) the artist of the album the cover art should be requested for, if available.
 * @param title (V1) the title contained in the album the cover art should be requested for,
 *                   if available.
 * @param album (V1) the name of the album the cover art should be requested for, if available.
 * @param lifeCycleData (V2) the life cycle data of a cover art request data.
 */

data class CoverArtRequestDataEntity(
    var artist: String? = null,
    var title: String? = null,
    var album: String? = null,
    var lifeCycleData: LifeCycleDataEntity? = null
)
