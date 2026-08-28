package technology.cariad.entertainment.metadataservice.data.database.entity

import technology.cariad.entertainment.metadataservice.sampledata.SampleStationMetadata
import org.junit.Assert
import org.junit.Test

class StationMetadataEntryTest {

    @Test
    fun stationMetadataEntryConstructor_shouldCreateEntry() {
        Assert.assertEquals(1L, SampleStationMetadata.STATION_METADATA_ENTRY_1.metadataId)
        Assert.assertEquals(2L, SampleStationMetadata.STATION_METADATA_ENTRY_1.stationId)
        Assert.assertEquals("short_name", SampleStationMetadata.STATION_METADATA_ENTRY_1.shortName)
        Assert.assertEquals("long_name", SampleStationMetadata.STATION_METADATA_ENTRY_1.longName)
        Assert.assertEquals("medium_name", SampleStationMetadata.STATION_METADATA_ENTRY_1.mediumName)
        Assert.assertEquals("description", SampleStationMetadata.STATION_METADATA_ENTRY_1.description)
    }
}
