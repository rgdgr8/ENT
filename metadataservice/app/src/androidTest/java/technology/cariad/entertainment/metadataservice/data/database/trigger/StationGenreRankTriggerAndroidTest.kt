package technology.cariad.entertainment.metadataservice.data.database.trigger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.GenreDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationGenreRankDao
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationGenreRankTriggerAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationGenreRankDao: StationGenreRankDao
    private lateinit var genreDao: GenreDao
    private lateinit var stationDao: StationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationGenreRankDao = database.getStationGenreRankDao()
        genreDao = database.getGenreDao()
        stationDao = database.getStationDao()
    }

    @Test
    fun deleteStationGenreRank_shouldDeleteGenreIfLastReferenced() = runBlocking {
        val genreEntry = GenreEntry(0, "genre")
        val genreId = genreDao.insert(genreEntry)

        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val stationGenreRankEntry = StationGenreRankEntry(stationId, genreId, 0)
        stationGenreRankDao.insert(stationGenreRankEntry)

        stationGenreRankDao.deleteByStationId(stationId)

        val genreIdAfterDelete = genreDao.findIdByName("genre")

        Assert.assertNull(genreIdAfterDelete)
    }

    @Test
    fun deleteStationGenreRank_shouldNotDeleteGenreIfReferencedByOtherEntries() = runBlocking {
        val genreEntry = GenreEntry(0, "genre")
        val genreId = genreDao.insert(genreEntry)

        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val stationGenreRankEntry = StationGenreRankEntry(stationId, genreId, 0)
        stationGenreRankDao.insert(stationGenreRankEntry)

        val otherStationEntry = StationEntry()
        val otherStationId = stationDao.insert(otherStationEntry)

        val otherStationGenreRankEntry = StationGenreRankEntry(otherStationId, genreId, 0)
        stationGenreRankDao.insert(otherStationGenreRankEntry)

        stationGenreRankDao.deleteByStationId(stationId)

        val genreIdAfterDelete = genreDao.findIdByName("genre")

        Assert.assertNotNull(genreIdAfterDelete)
    }

    @Test
    fun deleteStationGenreRank_shouldNotDeleteOtherGenres() = runBlocking {
        val genreEntry = GenreEntry(0, "genre")
        val genreId = genreDao.insert(genreEntry)

        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val stationGenreRankEntry = StationGenreRankEntry(stationId, genreId, 0)
        stationGenreRankDao.insert(stationGenreRankEntry)

        val otherGenreEntry = GenreEntry(0, "other_genre")
        genreDao.insert(otherGenreEntry)

        stationGenreRankDao.deleteByStationId(stationId)

        val otherGenreIdAfterDelete = genreDao.findIdByName("other_genre")

        Assert.assertNotNull(otherGenreIdAfterDelete)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

