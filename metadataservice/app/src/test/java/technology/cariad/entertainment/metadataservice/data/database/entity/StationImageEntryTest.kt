package technology.cariad.entertainment.metadataservice.data.database.entity

import technology.cariad.entertainment.metadataservice.sampledata.SampleImage.Companion.STATION_IMAGE_ENTRY_1
import org.junit.Assert
import org.junit.Test

class StationImageEntryTest {

    @Test
    fun albumEntryConstructor_shouldCreateEntry() {

        Assert.assertEquals(1L, STATION_IMAGE_ENTRY_1.imageId)
        Assert.assertEquals(2L, STATION_IMAGE_ENTRY_1.stationId)
    }
}
