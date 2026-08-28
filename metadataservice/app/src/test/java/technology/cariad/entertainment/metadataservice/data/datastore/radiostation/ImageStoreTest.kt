package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationImageDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleImage
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

private const val IMAGE_ID = 4L
private const val STATION_ID = 1L

class ImageStoreTest {

    lateinit var imageStore: ImageStore

    val stationImageDao = mockk<StationImageDao>(relaxed = true)
    val imageDao = mockk<ImageDao>(relaxed = true)
    val eTagsDao = mockk<ETagsDao>(relaxed = true)

    @Before
    fun setup() {
        LoggerMock()

        imageStore = ImageStore(stationImageDao, imageDao, eTagsDao)
    }

    @Test
    fun findLogoByStationIdAndType_shouldReturnSpecifiedImageIdIfExists() = runBlockingTest {

        val imageType = ImageEntity.Companion.ImageType.LOGO

        coEvery {
            stationImageDao.findLogoByStationIdAndType(any(), any())
        } returns IMAGE_ID

        val imageId = imageStore.findLogoByStationIdAndType(STATION_ID, imageType)

        Assert.assertEquals(IMAGE_ID, imageId)

        coVerify { stationImageDao.findLogoByStationIdAndType(STATION_ID, imageType) }
    }

    @Test
    fun findLogoByStationIdAndType_shouldReturnNullIfImageNotFound() = runBlockingTest {

        val imageType = ImageEntity.Companion.ImageType.COVER_ART

        coEvery {
            stationImageDao.findLogoByStationIdAndType(any(), any())
        } returns null

        val imageId = imageStore.findLogoByStationIdAndType(STATION_ID, imageType)

        Assert.assertEquals(null, imageId)

        coVerify { stationImageDao.findLogoByStationIdAndType(STATION_ID, imageType) }
    }

    @Test
    fun createStationImageEntry_shouldCreateStationImageEntry() = runBlockingTest {

        val expectedStationImageEntry = StationImageEntry(IMAGE_ID, STATION_ID)

        every {
            stationImageDao.createStationImageEntry(
                any(),
                any()
            )
        } returns expectedStationImageEntry

        val stationImageEntry = imageStore.createStationImageEntry(IMAGE_ID, STATION_ID)

        Assert.assertEquals(expectedStationImageEntry, stationImageEntry)

        verify(exactly = 1) { stationImageDao.createStationImageEntry(IMAGE_ID, STATION_ID) }
    }

    @Test
    fun delete_shouldDeleteSpecifiedStationImageEntry() = runBlockingTest {

        val stationImageEntry = StationImageEntry(IMAGE_ID, STATION_ID)

        coEvery { stationImageDao.delete(any()) } returns Unit

        imageStore.delete(stationImageEntry, ImageType.LOGO)

        coVerify { stationImageDao.delete(stationImageEntry) }
    }

    @Test
    fun updateLogo_shouldUpdateLogoOfSpecifiedStationIfLogoFound() = runBlockingTest {

        val imageType = ImageEntity.Companion.ImageType.LOGO
        val logoId = 1L
        val logo = ImageEntity(
            null,
            "fileName",
            imageType,
            null
        )
        val logoEntry = ImageEntry(
            0L,
            ImageEntity.Companion.ImageType.LOGO.value,
            "fileName"
        )

        coEvery { stationImageDao.findLogoByStationIdAndType(any(), any()) } returns logoId
        every { imageDao.createImageEntry(any(), any()) } returns logoEntry
        coEvery { imageDao.update(any<ImageEntry>()) } returns Unit

        imageStore.updateLogo(STATION_ID, logo)

        coVerify {
            stationImageDao.findLogoByStationIdAndType(STATION_ID, imageType)
            imageDao.update(logoEntry)
        }
        verify { imageDao.createImageEntry(logoId, logo) }
    }

    @Test
    fun updateLogo_shouldCreateAndInsertNewLogoIfLogoNotFound() = runBlockingTest {

        val imageType = ImageEntity.Companion.ImageType.LOGO
        val logo = ImageEntity(
            null,
            "fileName",
            ImageEntity.Companion.ImageType.LOGO,
            null
        )

        coEvery { stationImageDao.findLogoByStationIdAndType(any(), any()) } returns null
        every { imageDao.createImageEntry(any(), any()) } returns SampleImage.IMAGE_ENTRY_1
        coEvery { imageDao.insert(any<ImageEntry>()) } returns IMAGE_ID
        every { stationImageDao.createStationImageEntry(any(), any()) } answers { callOriginal() }
        coEvery { stationImageDao.insert(any<StationImageEntry>()) } returns IMAGE_ID

        imageStore.updateLogo(STATION_ID, logo)

        coVerify {
            stationImageDao.findLogoByStationIdAndType(STATION_ID, imageType)
            imageDao.insert(any<ImageEntry>())
            stationImageDao.insert(any<StationImageEntry>())
        }
        verify {
            imageDao.createImageEntry(0L, logo)
            stationImageDao.createStationImageEntry(IMAGE_ID, STATION_ID)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

