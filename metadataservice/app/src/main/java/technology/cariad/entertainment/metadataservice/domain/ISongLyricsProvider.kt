package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.SongLyricsEntity
import technology.cariad.entertainment.metadataservice.domain.value.LyricsType
import kotlinx.coroutines.flow.Flow

interface ISongLyricsProvider : IOnlineService {

    /**
     * Provides lyrics of the currently playing song.
     * @param lyricsType Type of Lyric to provide
     * @param familyFriendly Normal or family friendly lyrics
     * @return flow of [SongLyricsEntity]
     */
    fun getSongLyrics(lyricsType: LyricsType, familyFriendly: Boolean): Flow<SongLyricsEntity>

    /**
     * Sets the MCC for country specific lyrics requests.
     * @param mcc Mobile Country Code
     */
    fun setMcc(mcc: Int)
}
