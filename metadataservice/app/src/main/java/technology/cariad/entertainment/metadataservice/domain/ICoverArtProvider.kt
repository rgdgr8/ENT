package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity

interface ICoverArtProvider : IOnlineService {

    /**
     * Provides cover art from cache or online
     * @param coverArtInfo information for retrieving cover art data
     * @return [ImageEntity] containing cover art data
     */
    suspend fun getCoverArtImage(coverArtInfo: CoverArtRequestDataEntity): ImageEntity
}
