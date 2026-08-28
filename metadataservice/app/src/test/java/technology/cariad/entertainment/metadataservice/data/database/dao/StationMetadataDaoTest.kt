package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.StationMetadataEntry
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationMetadata.Companion.STATION_METADATA_ENTITY_2
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

private const val STATION_ID = 1L

class StationMetadataDaoTest {

    private var stationMetadataDao: StationMetadataDao = mockk(relaxed = true)

    @Test
    fun createStationMetadataEntry_shouldConvertEntityToEntry() {

        every {
            stationMetadataDao.createStationMetadataEntry(any(), any(), any())
        } answers { callOriginal() }

        val stationMetadataEntry =
            stationMetadataDao.createStationMetadataEntry(
                metadataId = 0,
                metadata = STATION_METADATA_ENTITY_2,
                stationId = STATION_ID
            )

        val expectedMetadataEntry = StationMetadataEntry(
            metadataId = 0,
            shortName = "short_name_2",
            mediumName = "medium_name_2",
            longName = "long_name_2",
            description = "description_2",
            stationId = STATION_ID
        )

        Assert.assertEquals(expectedMetadataEntry, stationMetadataEntry)
    }
}

