package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.data.database.dao.BearerDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.data.database.sampledata.SampleBearer
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.coEvery
import io.mockk.coVerify
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

class BearerStoreTest {

    lateinit var bearerStore: BearerStore

    lateinit var bearer: IBearerEntity
    lateinit var bearerUri: String

    private val bearerDao: BearerDao = mockk(relaxed = true)
    private val eTagsDao: ETagsDao = mockk(relaxed = true)
    private val stationStore: StationStore = mockk(relaxed = true)

    @Before
    fun setup() {

        LoggerMock()

        bearer = SampleBearer.FM_BEARER_ENTITY_1
        bearerUri = bearer.toBearerString()!!

        coEvery { stationStore.getOrCreateStation(any()) } returns 0L
        every { bearerDao.createBearerEntries(any(), any()) } answers { callOriginal() }

        bearerStore = BearerStore(bearerDao, eTagsDao)
    }

    @Test
    fun getStationIdByBearer_shouldReturnStationIdIfStationFound() = runBlockingTest {

        coEvery { bearerDao.findStationIdByBearer(any()) } returns STATION_ID

        val stationId = bearerStore.getStationIdByBearer(SampleBearer.FM_BEARER_ENTITY_1)

        Assert.assertEquals(STATION_ID, stationId)

        coVerify { bearerDao.findStationIdByBearer(bearerUri) }
    }

    @Test
    fun getStationIdByBearer_shouldReturnNullIfStationNotFound() = runBlockingTest {

        coEvery { bearerDao.findStationIdByBearer(any()) } returns null

        val stationId = bearerStore.getStationIdByBearer(SampleBearer.FM_BEARER_ENTITY_1)

        Assert.assertEquals(null, stationId)

        coVerify { bearerDao.findStationIdByBearer(bearerUri) }
    }

    @Test
    fun removeBearersByStationId_shouldDeleteBearersOfStation() = runBlockingTest {

        coEvery { bearerDao.deleteByStationId(any()) } returns 1

        bearerStore.removeBearersByStationId(STATION_ID)

        coVerify { bearerDao.deleteByStationId(STATION_ID) }
    }

    @Test
    fun insertBearers_shouldInsertBearersForStation() = runBlockingTest {

        val bearerList = BearerListEntity(listOf(
            SampleBearer.FM_BEARER_ENTITY_1,
            SampleBearer.DAB_BEARER_ENTITY_1,
            SampleBearer.IBOC_BEARER_ENTITY_1
        ), "etag")
        val bearerEntries = bearerDao.createBearerEntries(bearerList, STATION_ID)

        coEvery { bearerDao.insertIgnore(any<List<BearerEntry>>()) } returns listOf(1L, 2L, 3L)

        val bearerIds = bearerStore.insertBearers(STATION_ID, bearerList)

        Assert.assertEquals(listOf(1L, 2L, 3L), bearerIds)

        verify { bearerDao.createBearerEntries(bearerList, STATION_ID) }
        coVerify { bearerDao.insertIgnore(bearerEntries) }
    }

    @Test
    fun updateBearers_shouldDeleteMissingBearersAndInsertNewBearers() = runBlockingTest {

        val bearerList = BearerListEntity(listOf(
            SampleBearer.FK_BEARER_ENTITY_1,
            SampleBearer.DAB_BEARER_ENTITY_1,
            SampleBearer.IBOC_BEARER_ENTITY_1
        ), "etag")
        val uris = bearerList.bearers.mapNotNull { it.toBearerString() }
        val bearerEntries = bearerDao.createBearerEntries(bearerList, STATION_ID)

        coEvery { bearerDao.deleteMissingBearersForStationId(any(), any()) } returns Unit
        coEvery { bearerDao.insertIgnore(any<List<BearerEntry>>()) } returns listOf(1L, 2L, 3L)

        bearerStore.updateBearers(STATION_ID, bearerList)

        coVerify {
            bearerDao.deleteMissingBearersForStationId(STATION_ID, uris)
            bearerDao.insertIgnore(bearerEntries)
        }
    }

    @Test
    fun updateBearers_shouldDeleteAllBearersOfStationAndInsertNothing() = runBlockingTest {

        val bearerList = BearerListEntity(listOf(
            SampleBearer.FK_BEARER_ENTITY_1,
            SampleBearer.FK_BEARER_ENTITY_2,
            SampleBearer.FK_BEARER_ENTITY_3
        ), "etag")
        val uris = bearerList.bearers.mapNotNull { it.toBearerString() }
        val bearerEntries = bearerDao.createBearerEntries(bearerList, STATION_ID)

        coEvery { bearerDao.deleteMissingBearersForStationId(any(), any()) } returns Unit
        coEvery { bearerDao.insertIgnore(any<List<BearerEntry>>()) } returns listOf<Long>()
        val insertedBearers = bearerDao.insertIgnore(bearerEntries)

        bearerStore.updateBearers(STATION_ID, bearerList)

        Assert.assertEquals(listOf<String>(), uris)
        Assert.assertEquals(listOf<BearerEntry>(), bearerEntries)
        Assert.assertEquals(listOf<Long>(), insertedBearers)

        coVerify {
            bearerDao.deleteMissingBearersForStationId(STATION_ID, uris)
            bearerDao.insertIgnore(bearerEntries)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

