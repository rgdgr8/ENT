package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity

/**
 * Use case for retrieving album information.
 */
class RetrieveRecordingMetadataById(private val musicMetadataRepository: IMusicMetadataRepository) {

    /**
     * Provides album information.
     * @return
     */
    suspend fun retrieveRecordingMetadata(recordingId: String): MusicMetadataEntity {
        return musicMetadataRepository.getRecordingMetadata(recordingId)
    }
}
