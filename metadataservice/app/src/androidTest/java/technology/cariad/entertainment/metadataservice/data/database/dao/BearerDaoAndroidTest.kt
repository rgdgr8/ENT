package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BearerDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var bearerDao: BearerDao
    private lateinit var lifeCycleDataDao: LifeCycleDataDao
    private var expectedStationId = 0L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        bearerDao = database.getBearerDao()
        lifeCycleDataDao = database.getLifeCycleDataDao()


        runBlocking {
            val lifeCycleDataId = lifeCycleDataDao.insert(LifeCycleDataEntry(0, 500))
            expectedStationId = database.getStationDao().insert(StationEntry(lifeCycleDataId = lifeCycleDataId))
            bearerDao.insert(BearerEntry(0, expectedStationId, SampleBearer.FM_BEARER_STRING_1))
        }
    }

    @Test
    fun createBearerEntries_shouldCreateEmptyListForEmptyInput() {
        val bearerEntries = bearerDao.createBearerEntries(BearerListEntity(listOf(), null), 1)
        Assert.assertEquals(listOf<BearerEntry>(), bearerEntries)
    }

    @Test
    fun findStationIdByBearer_shouldReturnStationIdForExistingBearer() = runBlocking {
        val stationId = bearerDao.findStationIdByBearer(SampleBearer.FM_BEARER_STRING_1)
        Assert.assertEquals(expectedStationId, stationId)
    }

    @Test
    fun findStationIdByBearer_shouldReturnNullForMissingBearer() = runBlocking {
        val stationId = bearerDao.findStationIdByBearer(SampleBearer.FM_BEARER_STRING_2)
        Assert.assertNull(stationId)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

