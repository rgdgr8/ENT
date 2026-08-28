package technology.cariad.entertainment.metadataservice.data.database.trigger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationImageDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationImageTriggerAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var stationImageDao: StationImageDao
    private lateinit var imageDao: ImageDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationDao = database.getStationDao()
        stationImageDao = database.getStationImageDao()
        imageDao = database.getImageDao()
    }

    @Test
    fun deleteStationImage_shouldDeleteReferencedImage() = runBlocking {
        val stationEntry = StationEntry(0, null)
        val stationId = stationDao.insert(stationEntry)

        val imageEntry = ImageEntry(0, ImageEntity.Companion.ImageType.LOGO.value, "image")
        val imageId = imageDao.insert(imageEntry)

        val stationImageEntry = StationImageEntry(imageId, stationId)
        stationImageDao.insert(stationImageEntry)

        stationImageDao.deleteImageByStationId(stationId)

        val imageAfterDelete = imageDao.findImageById(imageId)

        Assert.assertNull(imageAfterDelete)
    }

    @Test
    fun deleteStationImage_shouldNotDeleteOtherImages() = runBlocking {
        val stationEntry = StationEntry(0, null)
        val stationId = stationDao.insert(stationEntry)

        val imageEntry = ImageEntry(0, ImageEntity.Companion.ImageType.LOGO.value, "image")
        val imageId = imageDao.insert(imageEntry)

        val otherImageEntry =
            ImageEntry(0, ImageEntity.Companion.ImageType.LOGO.value, "other_image")
        val otherImageId = imageDao.insert(otherImageEntry)

        stationImageDao.insert(StationImageEntry(imageId, stationId))

        stationImageDao.deleteImageByStationId(stationId)

        val otherImage = imageDao.findImageById(otherImageId)

        Assert.assertNotNull(otherImage)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

