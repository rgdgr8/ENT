package technology.cariad.entertainment.metadataservice.data.database.entity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.GenreDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationGenreRankDao
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationGenreRankEntryAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var genreDao: GenreDao
    private lateinit var stationGenreRankDao: StationGenreRankDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationDao = database.getStationDao()
        genreDao = database.getGenreDao()
        stationGenreRankDao = database.getStationGenreRankDao()
    }

    @Test
    fun deleteStation_shouldCascadeDeleteGenreRank() = runBlocking {
        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val genreEntry = GenreEntry(name = "genre")
        val genreId = genreDao.insert(genreEntry)

        val stationGenreRankEntry = StationGenreRankEntry(stationId, genreId, 0)
        stationGenreRankDao.insert(stationGenreRankEntry)

        stationDao.deleteByStationId(stationId)

        val deleteCount = stationGenreRankDao.deleteByStationId(stationId)

        Assert.assertEquals(0, deleteCount)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

