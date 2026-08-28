package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.domain.entity.SongLyricsEntity
import technology.cariad.entertainment.metadataservice.domain.value.LyricsType
import kotlinx.coroutines.flow.Flow

interface ISongLyricsRepository {

    /**
     * Retrieves lyrics for the currently playing song.
     * @param lyricsType Type of lyrics as defined in [LyricsType]
     * @param familyFriendly Indicates if the familiy friendly or original version of the lyrics
     *                       should be fetched.
     */
    fun retrieveSongLyrics(
        lyricsType: LyricsType,
        familyFriendly: Boolean
    ): Flow<Result<SongLyricsEntity>>
}

