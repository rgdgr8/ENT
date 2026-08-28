package technology.cariad.entertainment.metadataservice.service.musicmetadata

import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.RetrieveRecordingMetadataById

class RetrieveRecordingMetadataByIdFactory(
    private val musicMetadataRepository: IMusicMetadataRepository
) {
    /**
     * Creates a new [RetrieveRecordingMetadataById] use case.
     */
    fun create() = RetrieveRecordingMetadataById(musicMetadataRepository)
}

