package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo

class SampleSongInfo {
    companion object {
        val RECOGNIZED_SONG_INFO_1 = RecognizedSongInfo(
            "artist",
            "title",
            "album",
            "year",
            "cover_art_uri",
            "era",
            "genre",
            "mood",
            "tempo",
            listOf(SampleExternalIdentifier.EXTERNAL_IDENTIFIER_AMAZON_1),
            "gracenoteId",
            "gracenoteUniqueId",
            listOf(SampleExternalIdentifier.EXTERNAL_IDENTIFIER_AMAZON_1),
        )

        val SONG_INFO_ENTITY_1 = SongInfoEntity(
            "artist",
            "title",
            "album",
            "year",
            "cover_art_uri",
            "era",
            "genre",
            "mood",
            "tempo",
            listOf(SampleExternalIdentifier.EXTERNAL_IDENTIFIER_ENTITY_AMAZON_1),
            listOf(SampleExternalIdentifier.EXTERNAL_IDENTIFIER_ENTITY_AMAZON_1),
            "gracenoteId",
            "gracenoteUniqueId"
        )

        val SONG_INFO_ENTITY_2 = SongInfoEntity(
            artist = "Madonna",
            title = "Spanish Eyes",
            album = "Like a Prayer",
            year = "1989",
            coverartUri = null,
            era = null,
            genre = "Pop",
            mood = null,
            tempo = null,
            externalIds = listOf(),
            externalAlbumIds = listOf(),
            gracenoteId = "gracenoteId",
            gracenoteUniqueId = "gracenoteUniqueId"
        )

        val SONG_INFO_ENTITY_3 = SongInfoEntity(
            artist = "Adele",
            title = "Chasing Pavements",
            album = "19",
            year = "2008",
            coverartUri = null,
            era = null,
            genre = "Pop",
            mood = null,
            tempo = null,
            externalIds = listOfNotNull(null),
            externalAlbumIds = listOfNotNull(null),
            gracenoteId = "gracenoteId",
            gracenoteUniqueId = "gracenoteUniqueId"
        )
    }
}

