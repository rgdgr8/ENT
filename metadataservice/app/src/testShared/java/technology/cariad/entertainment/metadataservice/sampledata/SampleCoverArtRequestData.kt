package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData

class SampleCoverArtRequestData {
    companion object {
        val COVER_ART_REQUEST_DATA_1 = CoverArtRequestData(
            "Artist", "Title", "Album"
        )
        val COVER_ART_REQUEST_DATA_ENTITY_1 = CoverArtRequestDataEntity(
            "Artist", "Title", "Album", null
        )
        val COVER_ART_REQUEST_DATA_ENTITY_2 = CoverArtRequestDataEntity(
            "Artist", "Title", "Album", SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
        )
        val COVER_ART_REQUEST_DATA_ENTITY_3 = CoverArtRequestDataEntity(
            "Artist", "Title", "Album", SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
        )
        val COVER_ART_REQUEST_DATA_ENTITY_4 = CoverArtRequestDataEntity(
            null, "Title", null, SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
        )
    }
}

