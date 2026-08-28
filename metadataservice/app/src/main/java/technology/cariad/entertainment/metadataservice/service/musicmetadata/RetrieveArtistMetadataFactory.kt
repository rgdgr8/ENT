package technology.cariad.entertainment.metadataservice.service.musicmetadata

import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.RetrieveArtistMetadataById

class RetrieveArtistMetadataFactory(
    private val musicMetadataRepository: IMusicMetadataRepository
) {
    /**
     * Creates a new [RetrieveArtistMetadataById] use case.
     */
    fun create() = RetrieveArtistMetadataById(musicMetadataRepository)
}

