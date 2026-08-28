package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity

/**
 * Use case for retrieving album information.
 */
class RetrieveAlbumMetadataById(private val musicMetadataRepository: IMusicMetadataRepository) {

    /**
     * Provides album information.
     * @return
     */
    suspend fun retrieveAlbumMetadata(albumId: String): MusicMetadataEntity {
        return musicMetadataRepository.getAlbumMetadata(albumId)
    }
}

