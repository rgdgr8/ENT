package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

private val FAKE_STATION_IMAGE_ENTRY = StationImageEntry(
    imageId = 2,
    stationId = 7
)

class StationImageDaoTest {

    private val stationImageDao: StationImageDao = mockk()

    @Test
    fun createStationImageEntry_shouldCreateEntryByParams() {

        every { stationImageDao.createStationImageEntry(any(), any()) } answers { callOriginal() }

        val stationImageEntry = stationImageDao.createStationImageEntry(
            imageId = 2,
            stationId = 7
        )
        Assert.assertEquals(FAKE_STATION_IMAGE_ENTRY, stationImageEntry)
    }
}


