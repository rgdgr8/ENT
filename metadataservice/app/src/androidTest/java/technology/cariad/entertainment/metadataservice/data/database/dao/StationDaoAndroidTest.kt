package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var lifeCycleDataDao: LifeCycleDataDao
    private var expectedStationId = 0L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        stationDao = database.getStationDao()
        lifeCycleDataDao = database.getLifeCycleDataDao()

        runBlocking {
            val lifeCycleId = lifeCycleDataDao.insert(LifeCycleDataEntry(0, 500))
            expectedStationId = stationDao.insert(StationEntry(stationId = 0, lifeCycleDataId = lifeCycleId))
            lifeCycleDataDao.insert(LifeCycleDataEntry(0, 200))
        }
    }

    @Test
    fun findById_shouldReturnStation() = runBlocking {
        val station = stationDao.findCompleteById(expectedStationId)

        Assert.assertNotNull(station)
    }

    @Test
    fun findById_shouldReturnNullForMissingStation() = runBlocking {
        val station = stationDao.findCompleteById(42)

        Assert.assertNull(station)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

