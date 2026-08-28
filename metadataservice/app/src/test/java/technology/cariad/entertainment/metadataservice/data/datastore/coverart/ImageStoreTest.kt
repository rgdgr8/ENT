package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleImage
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

class ImageStoreTest {
    private val imageDaoMock: ImageDao = mockk()

    private val imageStore = ImageStore(imageDaoMock)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun createImageEntry_shouldForwardResultFromDao() {
        every { imageDaoMock.createImageEntry(any(), any()) } returns SampleImage.IMAGE_ENTRY_1

        val imageEntry = imageStore.createImageEntry(SampleImage.IMAGE_ENTITY_1)

        Assert.assertEquals(SampleImage.IMAGE_ENTRY_1, imageEntry)
    }

    @Test
    fun insertImage_shouldForwardResultFromDao() = runBlockingTest {
        coEvery { imageDaoMock.insert(any<ImageEntry>()) } returns 1

        val imageId = imageStore.insertImage(SampleImage.IMAGE_ENTRY_1)

        Assert.assertEquals(1, imageId)
    }

    @Test
    fun deleteById_shouldForwardDeleteToDao() = runBlockingTest {
        coEvery { imageDaoMock.deleteById(any()) } returns Unit

        imageStore.deleteById(0L)

        coVerify { imageDaoMock.deleteById(0L) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

