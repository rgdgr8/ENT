package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.domain.ISongInfoRepository
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsRepository
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import technology.cariad.entertainment.metadataservice.domain.entity.SongLyricsEntity
import technology.cariad.entertainment.metadataservice.domain.value.LyricsType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion

/**
 * Use case for retrieving song lyrics.
 */
class RetrieveSongLyrics(
    private val songInfoRepository: ISongInfoRepository,
    private val songLyricsRepository: ISongLyricsRepository
) {

    private val log by logger()

    /**
     * Provides song lyrics.
     * @return a flow of [SongLyricsEntity]
     */
    fun songLyrics(
        lyricsType: LyricsType,
        familyFriendly: Boolean
    ): Flow<Result<SongLyricsEntity>> {
        var first = true
        return songInfoRepository.retrieveSongInfo(true).filter {
            val notEmpty = (it as? Success)?.result != SongInfoEntity()
            if (first && notEmpty) {
                log.info(this@RetrieveSongLyrics::songLyrics.name) {
                    "received first song recognition result $it"
                }
                first = false
                true
            } else {
                log.info(this@RetrieveSongLyrics::songLyrics.name) { "dropping song info $it" }
                false
            }
        }.flatMapLatest {
            log.info(this@RetrieveSongLyrics::songLyrics.name) {
                "start song lyrics request"
            }

            songLyricsRepository.retrieveSongLyrics(lyricsType, familyFriendly)
        }.onCompletion {
            log.info(this@RetrieveSongLyrics::songLyrics.name) {
                "finished song lyrics request"
            }
        }
    }
}
