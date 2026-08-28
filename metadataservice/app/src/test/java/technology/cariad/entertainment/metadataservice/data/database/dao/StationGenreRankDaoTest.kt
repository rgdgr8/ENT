package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class StationGenreRankDaoTest {

    private var stationGenreRankDao: StationGenreRankDao = mockk()

    @Test
    fun createGenreRankEntry_shouldConvertEntityToEntry() {

        every {
            stationGenreRankDao.createGenreRankEntry(any(), any(), any())
        } answers { callOriginal() }

        val genreRankEntry = stationGenreRankDao.createGenreRankEntry(1, 2, 3)

        val expectedGenreRankEntry = StationGenreRankEntry(1, 2, 3)

        Assert.assertEquals(expectedGenreRankEntry, genreRankEntry)
    }
}

