package technology.cariad.entertainment.metadataservice.service.musicmetadata

import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.RetrieveAlbumMetadataById

class RetrieveAlbumMetadataFactory(
    private val musicMetadataRepository: IMusicMetadataRepository
) {
    /**
     * Creates a new [RetrieveAlbumMetadataById] use case.
     */
    fun create() = RetrieveAlbumMetadataById(musicMetadataRepository)
}

