package technology.cariad.entertainment.metadataservice.service.coverart

import technology.cariad.entertainment.metadataservice.domain.ICoverArtRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.TextBasedCoverArtLookup

class TextBasedCoverArtLookupFactory(private val coverArtRepository: ICoverArtRepository) {

    /**
     * Creates a new [TextBasedCoverArtLookup] use case.
     */
    fun create() =  TextBasedCoverArtLookup(coverArtRepository)
}
