package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import android.net.Uri
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationImageDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var imageDao: ImageDao
    private lateinit var stationImageDao: StationImageDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        imageDao = database.getImageDao()
        stationImageDao = database.getStationImageDao()
    }

    @Test
    fun createImageEntry_shouldConvertEntityToEntry() {
        val imageEntry = imageDao.createImageEntry(
            0,
            ImageEntity(
                null, "logo.png", ImageEntity.Companion.ImageType.LOGO, null
            )
        )

        val stationImageEntry = stationImageDao.createStationImageEntry(imageEntry.imageId, 1)

        val expectedEntry =
            ImageEntry(0, ImageEntity.Companion.ImageType.LOGO.value, "logo.png")

        val expectedStationImageEntry =
            StationImageEntry(0, 1)

        Assert.assertEquals(expectedEntry, imageEntry)
        Assert.assertEquals(expectedStationImageEntry, stationImageEntry)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

