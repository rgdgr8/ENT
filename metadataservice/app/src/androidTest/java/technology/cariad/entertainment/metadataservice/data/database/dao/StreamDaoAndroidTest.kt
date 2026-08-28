package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationStream
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StreamDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var lifeCycleDataDao: LifeCycleDataDao
    private lateinit var streamDao: StreamDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        stationDao = database.getStationDao()
        lifeCycleDataDao = database.getLifeCycleDataDao()
        streamDao = database.getStreamDao()
    }

    @Test
    fun deleteByStationId_shouldRemoveAllStreamsOfThisStation() = runBlocking {

        val lifecycleId = lifeCycleDataDao.insert(LifeCycleDataEntry(lastRequested = 0))
        val stationId = stationDao.insert(StationEntry(lifeCycleDataId = lifecycleId))

        val streamEntries = listOf(
            SampleStationStream.STATION_STREAM_ENTITY_1,
            SampleStationStream.STATION_STREAM_ENTITY_2
        ).mapIndexed { index, station ->
            streamDao.createStreamEntry(station, stationId, index)
        }

        streamDao.insert(streamEntries)
        streamDao.deleteByStationId(stationId)

        val station = stationDao.findCompleteById(stationId)

        Assert.assertEquals(listOf<StreamEntry>(), station!!.streams)
    }

    @Test
    fun deleteByStationId_shouldNotRemoveAnyStreamsOfOtherStations() = runBlocking {

        val lifecycleIdThis = lifeCycleDataDao.insert(LifeCycleDataEntry(lastRequested = 0))
        val stationIdThis = stationDao.insert(StationEntry(lifeCycleDataId = lifecycleIdThis))

        val lifecycleIdOther = lifeCycleDataDao.insert(LifeCycleDataEntry(lastRequested = 0))
        val stationIdOther = stationDao.insert(StationEntry(lifeCycleDataId = lifecycleIdOther))

        val streamEntriesOther = listOf(
            SampleStationStream.STATION_STREAM_ENTITY_1,
            SampleStationStream.STATION_STREAM_ENTITY_2
        ).mapIndexed { index, station ->
            streamDao.createStreamEntry(station, stationIdOther, index)
        }

        streamDao.insert(streamEntriesOther)

        streamDao.deleteByStationId(stationIdThis)

        val stationOther = stationDao.findCompleteById(stationIdOther)

        Assert.assertEquals(2, stationOther!!.streams.size)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insert_shouldThrowIfPositionsAreNotUniquePerStation() {
        runBlocking {

            val lifecycleId = lifeCycleDataDao.insert(LifeCycleDataEntry(lastRequested = 0))
            val stationId = stationDao.insert(StationEntry(lifeCycleDataId = lifecycleId))

            val streams = listOf(
                StreamEntry(
                    0,
                    stationId,
                    0,
                    "uri_one",
                    0,
                    StationStreamEntity.Companion.QualityType.HIGH
                ),
                StreamEntry(
                    0,
                    stationId,
                    0,
                    "uri_two",
                    0,
                    StationStreamEntity.Companion.QualityType.LOW
                )
            )

            streamDao.insert(streams)
        }
    }

    @After
    fun tearDown() {
        database.close()
    }
}

