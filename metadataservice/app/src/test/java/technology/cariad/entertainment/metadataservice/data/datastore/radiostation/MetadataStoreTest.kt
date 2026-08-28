package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationMetadataDao
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationMetadata
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
private val METADATA_ID = 3

class MetadataStoreTest {

    private val stationMetadataDao: StationMetadataDao = mockk(relaxed = true)
    private val eTagsDao: ETagsDao = mockk(relaxed = true)

    lateinit var metadataStore: MetadataStore

    @Before
    fun setup() {
        LoggerMock()

        metadataStore = MetadataStore(stationMetadataDao, eTagsDao)
    }

    @Test
    fun deleteByStationId_shouldDeleteStationMetadata() = runBlockingTest {

        coEvery { stationMetadataDao.deleteByStationId(any()) } returns METADATA_ID

        metadataStore.deleteByStationId(STATION_ID)

        coVerify { stationMetadataDao.deleteByStationId(STATION_ID) }
    }

    @Test
    fun updateMetadata_shouldUpdateStationMetadata() = runBlockingTest {

        val stationMetaData = StationMetadataEntity(
            null,
            null,
            null,
            null,
            listOf("Rock", "Metal"),
            null
        )

        every {
            stationMetadataDao.createStationMetadataEntry(
                any(),
                any(),
                any()
            )
        } returns SampleStationMetadata.STATION_METADATA_ENTRY_1
        coEvery { stationMetadataDao.upsert(any()) } returns Unit

        metadataStore.updateMetadata(STATION_ID, stationMetaData)

        verify {
            stationMetadataDao.createStationMetadataEntry(
                any(),
                stationMetaData,
                STATION_ID
            )
        }
        coVerify { stationMetadataDao.upsert(SampleStationMetadata.STATION_METADATA_ENTRY_1) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

