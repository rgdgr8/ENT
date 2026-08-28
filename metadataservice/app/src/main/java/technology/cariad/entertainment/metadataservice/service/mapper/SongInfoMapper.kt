package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo

class SongInfoMapper : Mapper<SongInfoEntity, RecognizedSongInfo>() {
    override fun mapValue(value: SongInfoEntity): RecognizedSongInfo = RecognizedSongInfo(
        artist = value.artist,
        title = value.title,
        coverartUri = value.coverartUri,
        album = value.album,
        era = value.era,
        genre = value.genre,
        year = value.year,
        mood = value.mood,
        tempo = value.tempo,
        externalIds = ExternalIdentifierMapper().mapList(value.externalIds),
        externalAlbumIds = ExternalIdentifierMapper().mapList(value.externalAlbumIds),
        gracenoteId = value.gracenoteId,
        gracenoteUniqueId = value.gracenoteUniqueId
    )
}

