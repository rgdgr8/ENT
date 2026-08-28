package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.StationProviderIdentifierEntry
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class StationProviderIdentifierDaoTest {

    private var stationProviderIdentifierDao: StationProviderIdentifierDao = mockk()

    @Test
    fun createStationProviderIdentifierEntry_shouldConvertEntityToEntry() {

        every {
            stationProviderIdentifierDao.createStationProviderIdentifierEntry(any(), any())
        } answers { callOriginal() }

        val genreRankEntry = stationProviderIdentifierDao.createStationProviderIdentifierEntry(1, 2)
        val expectedGenreRankEntry = StationProviderIdentifierEntry(1, 2)

        Assert.assertEquals(expectedGenreRankEntry, genreRankEntry)
    }
}

