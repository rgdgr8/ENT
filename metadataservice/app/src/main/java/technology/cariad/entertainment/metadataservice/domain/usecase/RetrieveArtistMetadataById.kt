package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity

/**
 * Use case for retrieving artist information.
 */
class RetrieveArtistMetadataById(private val musicMetadataRepository: IMusicMetadataRepository) {

    /**
     * Provides artist information.
     * @return
     */
    suspend fun retrieveArtistMetadata(artistId: String): MusicMetadataEntity {
        return musicMetadataRepository.getArtistMetadata(artistId)
    }
}
