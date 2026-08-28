package technology.cariad.entertainment.metadataservice.service.songlyrics

import technology.cariad.entertainment.metadataservice.domain.ISongInfoRepository
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.RetrieveSongLyrics

class RetrieveSongLyricsFactory(
    private val songInfoRepository: ISongInfoRepository,
    private val songLyricsRepository: ISongLyricsRepository
) {

    /**
     * Creates a new [RetrieveSongLyrics] use case.
     */
    fun create() = RetrieveSongLyrics(songInfoRepository, songLyricsRepository)
}

