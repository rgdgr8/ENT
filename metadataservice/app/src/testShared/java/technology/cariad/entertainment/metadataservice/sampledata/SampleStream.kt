package de.esolutions.media.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity

private const val STATION_ID = 1L

class SampleStream {
    companion object {
        val STREAM_ENTRY_1 = StreamEntry(
            streamId = 0,
            stationId = STATION_ID,
            position = 0,
            url = "uri_1",
            offsetMs = 0,
            quality = StationStreamEntity.Companion.QualityType.HIGH
        )
        val STREAM_ENTRY_2 = StreamEntry(
            streamId = 1,
            stationId = STATION_ID,
            position = 1,
            url = "uri_2",
            offsetMs = 0,
            quality = StationStreamEntity.Companion.QualityType.LOW
        )
    }
}

