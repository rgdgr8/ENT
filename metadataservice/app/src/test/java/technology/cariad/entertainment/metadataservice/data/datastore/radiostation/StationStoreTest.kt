package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import de.esolutions.media.metadataservice.sampledata.SampleStream.Companion.STREAM_ENTRY_1
import de.esolutions.media.metadataservice.sampledata.SampleStream.Companion.STREAM_ENTRY_2
import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.data.database.DatabaseConstants
import technology.cariad.entertainment.metadataservice.data.database.dao.BearerDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationImageDao
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ETagsEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.pojo.CompleteRadioStation
import technology.cariad.entertainment.metadataservice.data.database.sampledata.SampleBearer
import technology.cariad.entertainment.metadataservice.data.filesystem.ImageDataStore
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleImage.Companion.IMAGE_ENTRY_3
import technology.cariad.entertainment.metadataservice.sampledata.SampleImage.Companion.IMAGE_ENTRY_4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val STATION_ID = 1L
private const val INVALID_DATA_INDEX = -1L

class StationStoreTest {

    private val bearer = SampleBearer.FM_BEARER_ENTITY_1
    private val bearerUri = bearer.toBearerString()!!
    private val streamsUnordered = listOf(STREAM_ENTRY_2, STREAM_ENTRY_1)
    private val completeRadioStation = CompleteRadioStation(
        providerIdentifiers = listOf(),
        genres = listOf(),
        bearers = listOf(),
        genreRanks = listOf(),
        images = listOf(),
        lifeCycleData = LifeCycleDataEntry(lastRequested = 0),
        station = StationEntry(0, 0),
        stationMetadata = null,
        streams = streamsUnordered,
        etags = ETagsEntry(0)
    )

    private val stationDao: StationDao = mockk(relaxed = true)
    private val bearerDao: BearerDao = mockk(relaxed = true)
    private val stationImageDao: StationImageDao = mockk(relaxed = true)
    private val imageDao: ImageDao = mockk(relaxed = true)
    private val eTagsDao: ETagsDao = mockk(relaxed = true)
    private val imageDataStoreMock: ImageDataStore = mockk(relaxed = true)

    private lateinit var stationStore: StationStore

    @Before
    fun setup() {
        LoggerMock()

        stationStore = StationStore(
            stationDao,
            bearerDao,
            stationImageDao,
            imageDao,
            imageDataStoreMock,
            eTagsDao
        )
    }

    @Test
    fun getOrCreateStation_shouldReturnStationIdIfFound() = runBlockingTest {

        coEvery { bearerDao.findStationIdByBearer(any()) } returns STATION_ID

        val stationId = stationStore.getOrCreateStation(bearer)

        Assert.assertEquals(STATION_ID, stationId)

        coVerify { bearerDao.findStationIdByBearer(bearerUri) }
    }

    @Test
    fun getOrCreateStation_shouldCreateStationIfNotFoundAndReturnItsId() = runBlockingTest {

        val stationStoreSpy = spyk(stationStore)

        coEvery { bearerDao.findStationIdByBearer(any()) } returns null
        coEvery { stationStoreSpy.createStation(any()) } returns STATION_ID

        val stationId = stationStoreSpy.getOrCreateStation(bearer)

        Assert.assertEquals(STATION_ID, stationId)

        coVerify {
            bearerDao.findStationIdByBearer(bearerUri)
            stationStoreSpy.createStation(bearerUri)
        }
    }

    @Test
    fun createStation_shouldOnlyCreateStationAndReturnItsIdIfCacheNotFull() = runBlockingTest {

        val validStationCount = 10
        val stationEntry = StationEntry(STATION_ID, null)
        val bearerEntry = BearerEntry(stationId = STATION_ID, uri = bearerUri)

        coEvery { stationDao.getCount() } returns validStationCount
        coEvery { stationDao.createStationEntry(any(), any()) } returns stationEntry
        coEvery { stationDao.insert(any<StationEntry>()) } returns STATION_ID
        coEvery { bearerDao.insert(any<BearerEntry>()) } returns 1

        val stationId = stationStore.createStation(bearerUri)

        Assert.assertEquals(STATION_ID, stationId)

        coVerify {
            stationDao.getCount()
            stationDao.createStationEntry()
            stationDao.insert(stationEntry)
            bearerDao.insert(bearerEntry)
        }
    }

    @Test
    fun createStation_shouldReturnErrorIfCacheFullAndOldestStationNotFount() = runBlockingTest {

        coEvery { stationDao.getCount() } returns DatabaseConstants.STATION_LOGO_CACHE_SIZE
        coEvery { stationDao.findLeastRecentlyUsedId() } returns null

        val stationId = stationStore.createStation(bearerUri)

        Assert.assertEquals(INVALID_DATA_INDEX, stationId)

        coVerify {
            stationDao.getCount()
            stationDao.findLeastRecentlyUsedId()
        }
        coVerify(inverse = true) { stationStore.deleteCompleteStation(any()) }
    }

    @Test
    fun createStation_shouldDeleteCompleteStationBeforeCreationIfCacheFullAndOldestStationFound() =
        runBlockingTest {

            val oldestStationId = 10L
            val bearerId = 1L

            val stationStoreSpy = spyk(stationStore)

            coEvery { stationDao.getCount() } returns DatabaseConstants.STATION_LOGO_CACHE_SIZE
            coEvery { stationDao.findLeastRecentlyUsedId() } returns oldestStationId
            coEvery { stationStoreSpy.deleteCompleteStation(any()) } returns true
            coEvery { stationDao.createStationEntry() } returns StationEntry()
            coEvery { stationDao.insert(any<StationEntry>()) } returns STATION_ID
            coEvery { bearerDao.insert(any<BearerEntry>()) } returns bearerId

            val stationId = stationStoreSpy.createStation(bearerUri)

            Assert.assertEquals(STATION_ID, stationId)

            coVerify {
                stationDao.getCount()
                stationDao.findLeastRecentlyUsedId()
                stationStoreSpy.deleteCompleteStation(oldestStationId)
                stationDao.createStationEntry()
                stationDao.insert(any<StationEntry>())
                bearerDao.insert(any<BearerEntry>())
            }
        }

    @Test
    fun deleteCompleteStation_shouldDeleteStationIfFound() = runBlockingTest {

        val imageIds = listOf<Long>(1L, 2L)
        val imageEntries = listOf<ImageEntry>(IMAGE_ENTRY_3, IMAGE_ENTRY_4)

        coEvery { stationImageDao.findImageIdsByStationId(STATION_ID) } returns imageIds
        coEvery { imageDao.findImageById(1L) } returns imageEntries[0]
        coEvery { imageDao.findImageById(2L) } returns imageEntries[1]
        every { imageDataStoreMock.deleteImage(any()) } returns true
        coEvery { stationDao.deleteByStationId(any()) } returns 1

        val stationId = 1L

        val result = stationStore.deleteCompleteStation(stationId)

        Assert.assertEquals(true, result)

        coVerify {
            stationImageDao.findImageIdsByStationId(STATION_ID)
            imageDao.findImageById(1L)
            imageDao.findImageById(2L)
            imageDataStoreMock.deleteImage("logo01.jpg")
            imageDataStoreMock.deleteImage("logo02.jpg")
            stationDao.deleteByStationId(stationId)
        }
    }

    @Test
    fun getCompleteById_shouldReturnStationIfFound() = runBlockingTest {

        coEvery { stationDao.findCompleteById(STATION_ID) } returns completeRadioStation

        val completeStation = stationStore.getCompleteById(STATION_ID)

        Assert.assertEquals(completeRadioStation, completeStation)

        coVerify {
            stationDao.findCompleteById(STATION_ID)
        }
    }

    @Test
    fun getCompleteById_shouldReturnNullIfNotFound() = runBlockingTest {

        coEvery { stationDao.findCompleteById(STATION_ID) } returns null

        val completeStation = stationStore.getCompleteById(STATION_ID)

        Assert.assertEquals(null, completeStation)

        coVerify {
            stationDao.findCompleteById(STATION_ID)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

