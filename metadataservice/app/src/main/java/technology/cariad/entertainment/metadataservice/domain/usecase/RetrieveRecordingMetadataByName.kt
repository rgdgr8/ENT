package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity

/**
 * Use case for retrieving album information.
 */
class RetrieveRecordingMetadataByName(
    private val musicMetadataRepository: IMusicMetadataRepository
) {

    /**
     * Provides album information.
     * @return
     */
    suspend fun retrieveRecordingMetadata(
        recordingName: String,
        artistName: String
    ): MusicMetadataEntity {
        return musicMetadataRepository.getRecordingMetadataByName(recordingName, artistName)
    }
}
