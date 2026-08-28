package technology.cariad.entertainment.metadataservice.data.database.trigger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.LifeCycleDataDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationTriggerAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var lifeCycleDataDao: LifeCycleDataDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationDao = database.getStationDao()
        lifeCycleDataDao = database.getLifeCycleDataDao()
    }

    @Test
    fun deleteStation_shouldDeleteReferencedLifeCycleData() = runBlocking {
        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val stationEntry = StationEntry(lifeCycleDataId = lifeCycleDataId)
        val stationId = stationDao.insert(stationEntry)

        stationDao.deleteByStationId(stationId)

        val lifeCycleDataAfterDelete = lifeCycleDataDao.findById(lifeCycleDataId)

        Assert.assertNull(lifeCycleDataAfterDelete)
    }

    @Test
    fun deleteStation_shouldNotDeleteOtherLifeCycleData() = runBlocking {
        val lifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)

        val stationEntry = StationEntry(lifeCycleDataId = lifeCycleDataId)
        val stationId = stationDao.insert(stationEntry)

        val otherLifeCycleDataEntry = LifeCycleDataEntry(lastRequested = 500)
        val otherLifeCycleDataId = lifeCycleDataDao.insert(otherLifeCycleDataEntry)

        stationDao.deleteByStationId(stationId)

        val otherLifeCycleDataAfterDelete = lifeCycleDataDao.findById(otherLifeCycleDataId)

        Assert.assertNotNull(otherLifeCycleDataAfterDelete)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

