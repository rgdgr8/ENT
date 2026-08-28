package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.LifeCycleDataDao
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleLifeCycleData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class LifeCycleStoreTest {
    private val lifeCycleDataDaoMock: LifeCycleDataDao = mockk()

    private val lifeCycleStore = LifeCycleStore(lifeCycleDataDaoMock)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun createLifeCycleDataEntry_shouldForwardResultFromDao() {
        every {
            lifeCycleDataDaoMock.createLifeCycleDataEntry(any(), any())
        } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1

        val lifeCycleDataEntry = lifeCycleStore.createLifeCycleDataEntry(
            lifeCycleDataEntity = SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
        )

        Assert.assertEquals(SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1, lifeCycleDataEntry)
    }

    @Test
    fun insertLifeCycleData_shouldForwardResultFromDao() = runBlockingTest {
        coEvery { lifeCycleDataDaoMock.insert(any<LifeCycleDataEntry>()) } returns 1L

        val lifeCycleDataId = lifeCycleStore.insertLifeCycleData(
            SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
        )

        Assert.assertEquals(1L, lifeCycleDataId)
    }

    @Test
    fun updateLifeCycleData_shouldForwardUpdateToDao() = runBlockingTest {
        coEvery { lifeCycleDataDaoMock.update(any<LifeCycleDataEntry>()) } returns Unit

        lifeCycleStore.updateLifeCycleData(SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1)

        coVerify {
            lifeCycleDataDaoMock.update(SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1)
        }
    }

    @Test
    fun deleteLifeCycleDataById_shouldForwardDeleteToDao() = runBlockingTest {
        coEvery { lifeCycleDataDaoMock.deleteById(any()) } returns Unit

        lifeCycleStore.deleteLifeCycleDataById(0L)

        coVerify { lifeCycleDataDaoMock.deleteById(0L) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

