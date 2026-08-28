package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.data.database.entity.StationMetadataEntry
import technology.cariad.entertainment.metadataserviceclient.api.StationMetadata
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity

class SampleStationMetadata {
    companion object {
        val STATION_METADATA_1 = StationMetadata(
            "short_name",
            "medium_name",
            "long_name",
            "description",
            listOf("genre")
        )
        val STATION_METADATA_ENTITY_1 = StationMetadataEntity(
            "short_name",
            "medium_name",
            "long_name",
            "description",
            listOf("genre"),
            "metadata_etag"
        )

        val STATION_METADATA_2 = StationMetadata(
            "short_name_2",
            "medium_name_2",
            "long_name_2",
            "description_2",
            listOf("genre_2")
        )
        val STATION_METADATA_ENTITY_2 = StationMetadataEntity(
            "short_name_2",
            "medium_name_2",
            "long_name_2",
            "description_2",
            listOf("genre_2"),
            "metadata_etag"
        )

        val STATION_METADATA_WITHOUT_GENRE_1 = StationMetadata(
            "short_name",
            "medium_name",
            "long_name",
            "description",
            null
        )
        val STATION_METADATA_ENTITIY_WITHOUT_GENRE_1 = StationMetadataEntity(
            "short_name",
            "medium_name",
            "long_name",
            "description",
            null,
            "metadata_etag"
        )
        val STATION_METADATA_ENTRY_1 = StationMetadataEntry(
            metadataId = 1L,
            stationId = 2L,
            description = "description",
            longName = "long_name",
            shortName = "short_name",
            mediumName = "medium_name"
        )
    }
}

