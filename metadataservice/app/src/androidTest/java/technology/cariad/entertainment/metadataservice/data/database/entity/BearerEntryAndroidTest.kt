package technology.cariad.entertainment.metadataservice.data.database.entity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.BearerDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BearerEntryAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var bearerDao: BearerDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationDao = database.getStationDao()
        bearerDao = database.getBearerDao()
    }

    @Test
    fun deleteStation_shouldCascadeDeleteBearers() = runBlocking {
        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val bearerEntry = BearerEntry(stationId = stationId, uri = "bearer_uri")
        bearerDao.insert(bearerEntry)

        stationDao.deleteByStationId(stationId)

        val stationIdAfterDelete = bearerDao.findStationIdByBearer("bearer_uri")
        
        Assert.assertNull(stationIdAfterDelete)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

