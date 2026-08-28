package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity.Companion.QualityType
import technology.cariad.entertainment.metadataserviceclient.api.StationStream

class SampleStationStream {
    companion object {
        val STATION_STREAM_1 = StationStream(
            "uri_1",
            0,
            StationStream.QUALITY_HIGH
        )
        val STATION_STREAM_ENTITY_1 = StationStreamEntity(
             "uri_1",
            0,
            QualityType.HIGH
        )

        val STATION_STREAM_2 = StationStream(
            "uri_2",
            0,
            StationStream.QUALITY_LOW
        )
        val STATION_STREAM_ENTITY_2 = StationStreamEntity(
            "uri_2",
            0,
            QualityType.LOW
        )
    }
}

