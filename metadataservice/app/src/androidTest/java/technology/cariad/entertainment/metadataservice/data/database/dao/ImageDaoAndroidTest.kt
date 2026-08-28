package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var imageDao: ImageDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        imageDao = database.getImageDao()
    }

    @Test
    fun createImageEntry_shouldConvertEntityToEntry() {
        val imageEntry = imageDao.createImageEntry(
            0,
            ImageEntity(null, "logo.png", ImageEntity.Companion.ImageType.LOGO, null)
        )

        val expectedEntry =
            ImageEntry(0, ImageEntity.Companion.ImageType.LOGO.value, "logo.png")

        Assert.assertEquals(expectedEntry, imageEntry)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

