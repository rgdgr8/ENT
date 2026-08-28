package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class ImageDaoTest {

    private var imageDao: ImageDao = mockk(relaxed = true)

    @Test
    fun createImageEntry_shouldConvertEntityToEntry() {

        every {
            imageDao.createImageEntry(
                any(),
                any()
            )
        } answers { callOriginal() }

        val imageEntry = imageDao.createImageEntry(
            0,
            ImageEntity(
                null,
                "logo.png",
                ImageEntity.Companion.ImageType.LOGO,
                null
            )
        )

        val expectedEntry =
            ImageEntry(
                0,
                ImageEntity.Companion.ImageType.LOGO.value,
                "logo.png"
            )

        Assert.assertEquals(expectedEntry, imageEntry)
    }
}

