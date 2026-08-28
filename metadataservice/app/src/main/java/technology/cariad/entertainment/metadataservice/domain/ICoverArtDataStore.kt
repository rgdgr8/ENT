package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity

/**
 * Interface for getting and adding cover art data from and to the cache.
 */
interface ICoverArtDataStore {

    /**
     * Checks the cache for cover art data referred to by [coverArtRequestDataEntity].
     * @param coverArtRequestDataEntity information for the cover art
     * @return [ImageEntity] if cover art found, otherwise null
     */
    suspend fun getCoverArt(coverArtRequestDataEntity: CoverArtRequestDataEntity): ImageEntity?

    /**
     * Adds cover art look up data to the cache.
     * @param coverArtRequestDataEntity information for the cover art
     * @param imageEntity data that should be added for the requested cover art
     */
    suspend fun addCoverArtLookup(
        coverArtRequestDataEntity: CoverArtRequestDataEntity,
        imageEntity: ImageEntity
    ): String?
}

