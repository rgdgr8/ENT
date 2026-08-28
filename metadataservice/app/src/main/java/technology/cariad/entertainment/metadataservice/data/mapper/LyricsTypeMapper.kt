package technology.cariad.entertainment.metadataservice.data.mapper

import technology.cariad.entertainment.metadataservice.data.provider.gracenote.GnLyricsType
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.value.LyricsType

class LyricsTypeMapper : Mapper<LyricsType, GnLyricsType>() {

    override fun mapValue(value: LyricsType): GnLyricsType {
        return when (value) {
            LyricsType.STATIC -> GnLyricsType.STATIC
            LyricsType.LINE_BY_LINE -> GnLyricsType.LINEBYLINE
            LyricsType.WORD_BY_WORD -> GnLyricsType.WORDBYWORD
        }
    }
}

