package technology.cariad.entertainment.metadataservice.service.songinfo

import technology.cariad.entertainment.metadataservice.domain.ISongInfoRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.RetrieveSongInfo

class RetrieveSongInfoFactory (private val songInfoRepository: ISongInfoRepository) {

    /**
     * Creates a new [RetrieveSongInfo] use case.
     */
    fun create() =  RetrieveSongInfo(songInfoRepository)
}
