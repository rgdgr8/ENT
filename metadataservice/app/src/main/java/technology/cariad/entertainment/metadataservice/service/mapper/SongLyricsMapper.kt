package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.SongLyricsEntity
import technology.cariad.entertainment.metadataserviceclient.api.SongLyrics

class SongLyricsMapper : Mapper<SongLyricsEntity, SongLyrics>() {
    override fun mapValue(value: SongLyricsEntity): SongLyrics = SongLyrics(
        value.lyricsJson,
        value.playPosition
    )
}

