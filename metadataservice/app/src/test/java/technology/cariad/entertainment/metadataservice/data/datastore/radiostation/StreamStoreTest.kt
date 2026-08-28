package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import de.esolutions.media.metadataservice.sampledata.SampleStream.Companion.STREAM_ENTRY_1
import de.esolutions.media.metadataservice.sampledata.SampleStream.Companion.STREAM_ENTRY_2
import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StreamDao
import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationStream.Companion.STATION_STREAM_ENTITY_1
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationStream.Companion.STATION_STREAM_ENTITY_2
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val STATION_ID = 1L

class StreamStoreTest {

    private val streamDao: StreamDao = mockk(relaxed = true)
    private val eTagsDao: ETagsDao = mockk(relaxed = true)

    private val streamStore = StreamStore(streamDao, eTagsDao)

    @Before
    fun setup() {
        LoggerMock()

        every { streamDao.createStreamEntry(any(), any(), any()) } answers { callOriginal() }
    }

    @Test
    fun deleteByStationId_shouldDeleteStreamsOfTheStation() = runBlockingTest {

        coEvery { streamDao.deleteByStationId(STATION_ID) } returns 3

        val deletedStreams = streamStore.deleteByStationId(STATION_ID)

        Assert.assertEquals(3, deletedStreams)

        coVerify { streamDao.deleteByStationId(STATION_ID) }
    }

    @Test
    fun insertStreams_shouldCreateStreamEntriesAndInsertThem() = runBlockingTest {
        val stationStreams = StationStreamListEntity(
            listOf(STATION_STREAM_ENTITY_1, STATION_STREAM_ENTITY_2),
            "etag"
        )
        val streamEntries = listOf(STREAM_ENTRY_1, STREAM_ENTRY_2)

        every {
            streamDao.createStreamEntry(any(), STATION_ID, any())
        } returns STREAM_ENTRY_1 andThen STREAM_ENTRY_2
        coEvery { streamDao.insert(streamEntries) } returns listOf(1L, 2L)

        streamStore.insertStreams(STATION_ID, stationStreams)

        verify {
            streamDao.createStreamEntry(STATION_STREAM_ENTITY_1, STATION_ID, 0)
            streamDao.createStreamEntry(STATION_STREAM_ENTITY_2, STATION_ID, 1)
        }
        coVerify { streamDao.insert(streamEntries) }
    }

    @Test
    fun updateStreams_shouldDeleteOldStreamsThenAddNewOnes() = runBlockingTest {
        val stationStreamList = StationStreamListEntity(
            listOf(STATION_STREAM_ENTITY_1, STATION_STREAM_ENTITY_2),
            "etag"
        )
        val streamEntries = stationStreamList.streams.mapIndexed { index, stream ->
            streamDao.createStreamEntry(stream, STATION_ID, index)
        }

        coEvery { streamDao.deleteByStationId(STATION_ID) } returns 2
        coEvery { streamDao.insert(streamEntries) } returns listOf(1L, 2L)

        streamStore.updateStreams(STATION_ID, stationStreamList)

        coVerifyOrder {
            streamDao.deleteByStationId(STATION_ID)
            streamDao.insert(streamEntries)
        }
    }

    @Test
    fun updateStreams_shouldDoNothingIfStreamsIsEmpty() = runBlockingTest {
        val stationStreamList = StationStreamListEntity(listOf(), "etag")
        val streamEntries = stationStreamList.streams.mapIndexed { index, stream ->
            streamDao.createStreamEntry(stream, STATION_ID, index)
        }

        streamStore.updateStreams(STATION_ID, stationStreamList)

        Assert.assertEquals(listOf<StreamEntry>(), streamEntries)

        coVerify(inverse = true) {
            streamDao.deleteByStationId(STATION_ID)
            streamDao.insert(streamEntries)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

