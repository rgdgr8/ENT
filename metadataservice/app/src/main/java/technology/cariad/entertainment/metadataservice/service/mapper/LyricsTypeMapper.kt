package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.value.LyricsType
import technology.cariad.entertainment.metadataserviceclient.api.SongLyrics.Companion.LYRICS_TYPE_FULL
import technology.cariad.entertainment.metadataserviceclient.api.SongLyrics.Companion.LYRICS_TYPE_LINE_BY_LINE
import technology.cariad.entertainment.metadataserviceclient.api.SongLyrics.Companion.LYRICS_TYPE_WORD_BY_WORD

class LyricsTypeMapper : Mapper<Int, LyricsType>() {
    override fun mapValue(value: Int): LyricsType =
        when (value) {
            LYRICS_TYPE_FULL -> LyricsType.STATIC
            LYRICS_TYPE_LINE_BY_LINE -> LyricsType.LINE_BY_LINE
            LYRICS_TYPE_WORD_BY_WORD -> LyricsType.WORD_BY_WORD
            else -> throw IllegalArgumentException("invalid lyrics type: value=$value")
        }
}

