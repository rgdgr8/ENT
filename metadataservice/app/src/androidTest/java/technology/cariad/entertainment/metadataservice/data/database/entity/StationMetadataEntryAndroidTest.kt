package technology.cariad.entertainment.metadataservice.data.database.entity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationMetadataDao
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationMetadataEntryAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var stationMetadataDao: StationMetadataDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationDao = database.getStationDao()
        stationMetadataDao = database.getStationMetadataDao()
    }

    @Test
    fun deleteStation_shouldCascadeDeleteMetadata() = runBlocking {
        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val stationMetadataEntry = StationMetadataEntry(metadataId = 0, stationId = stationId)
        stationMetadataDao.insert(stationMetadataEntry)

        stationDao.deleteByStationId(stationId)

        val stationMetadataAfterDelete = stationMetadataDao.findByStationId(stationId)

        Assert.assertNull(stationMetadataAfterDelete)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

