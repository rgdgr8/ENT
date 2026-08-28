package technology.cariad.entertainment.metadataservice.data.database.entity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationImageDao
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationImageEntryAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var imageDao: ImageDao
    private lateinit var stationImageDao: StationImageDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationDao = database.getStationDao()
        imageDao = database.getImageDao()
        stationImageDao = database.getStationImageDao()
    }

    @Test
    fun deleteStation_shouldCascadeDeleteStationImage() = runBlocking {
        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val imageEntry = ImageEntry(imageType = 1, fileName = "image.png")
        val imageId = imageDao.insert(imageEntry)

        val stationImageEntry = StationImageEntry(imageId, stationId)
        stationImageDao.insert(stationImageEntry)

        stationDao.deleteByStationId(stationId)

        val count = stationImageDao.findImageIdsByStationId(stationId).size

        Assert.assertEquals(0, count)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

