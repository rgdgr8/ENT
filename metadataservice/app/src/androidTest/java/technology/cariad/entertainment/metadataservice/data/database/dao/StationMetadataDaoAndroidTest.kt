package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.StationMetadataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationDataDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var stationMetadataDao: StationMetadataDao
    private var stationId = 0L
    private var metadataId = 1L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        stationDao = database.getStationDao()
        stationMetadataDao = database.getStationMetadataDao()

        runBlocking {
            stationId = stationDao.insert(StationEntry())
        }
    }

    @Test
    fun findById_shouldReturnStation() = runBlocking {
        stationMetadataDao.insert(
            StationMetadataEntry(
                metadataId,
                stationId,
                "shortName",
                "mediumName",
                "longName",
                "description"
            )
        )
        val station = stationMetadataDao.findByStationId(stationId)

        Assert.assertNotNull(station)
    }

    @Test
    fun findById_shouldReturnNullForMissingStationMetadata() = runBlocking {
        val stationMetadata = stationMetadataDao.findByStationId(42)

        Assert.assertNull(stationMetadata)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

