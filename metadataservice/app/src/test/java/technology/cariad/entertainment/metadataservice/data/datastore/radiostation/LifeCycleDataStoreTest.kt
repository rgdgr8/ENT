package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.LifeCycleDataDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleLifeCycleData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private const val STATION_ID = 1L
private const val LIFE_CYCLE_DATA_ID = 1L

class LifeCycleDataStoreTest {

    lateinit var lifeCycleData: LifeCycleDataEntry

    private val stationDao: StationDao = mockk(relaxed = true)
    private val lifeCycleDataDao: LifeCycleDataDao = mockk(relaxed = true)
    private val stationEntry: StationEntry = mockk(relaxed = true)

    lateinit var lifeCycleDataStore: LifeCycleDataStore

    @Before
    fun setup() {

        LoggerMock()

        lifeCycleDataStore = LifeCycleDataStore(lifeCycleDataDao, stationDao)

        lifeCycleData = LifeCycleDataEntry(lifeCycleDataId = 2, lastRequested = 987325)
    }

    @Test
    fun updateLifeCycleData_shouldUpdateLifeCycleDataIfStationFoundAndLifeCycleDataFound() =
        runBlockingTest {

            coEvery { stationDao.findByStationId(STATION_ID) } returns stationEntry
            every { stationEntry.lifeCycleDataId } returns 2L
            every {
                lifeCycleDataDao.createLifeCycleDataEntry(
                    any(),
                    any()
                )
            } answers { callOriginal() }
            coEvery { lifeCycleDataDao.update(any<LifeCycleDataEntry>()) } returns Unit

            lifeCycleDataStore.updateLifeCycleData(
                STATION_ID,
                SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
            )

            coVerify {
                stationDao.findByStationId(STATION_ID)
                lifeCycleDataDao.update(any<LifeCycleDataEntry>())
            }
            verify {
                stationEntry.lifeCycleDataId
                lifeCycleDataDao.createLifeCycleDataEntry(
                    2L,
                    SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
                )
            }
        }

    @Test
    fun updateLifeCycleData_shouldCreateAndInsertNewLifeCycleDataIfItNotFound() = runBlockingTest {

        coEvery { stationDao.findByStationId(STATION_ID) } returns stationEntry
        every { stationEntry.lifeCycleDataId } returns null
        every {
            lifeCycleDataDao.createLifeCycleDataEntry(
                any(),
                any()
            )
        } answers { callOriginal() }
        coEvery { lifeCycleDataDao.insert(any<LifeCycleDataEntry>()) } returns LIFE_CYCLE_DATA_ID
        every { stationDao.createStationEntry(any(), any()) } returns stationEntry
        coEvery { stationDao.update(stationEntry) } returns Unit

        lifeCycleDataStore.updateLifeCycleData(
            STATION_ID,
            SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
        )

        coVerify {
            stationDao.findByStationId(any())
            lifeCycleDataDao.insert(any<LifeCycleDataEntry>())
            stationDao.update(any<StationEntry>())
        }
        verify {
            stationEntry.lifeCycleDataId
            lifeCycleDataDao.createLifeCycleDataEntry(any(), any())
            stationDao.createStationEntry(any(), any())
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

