package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.filesystem.ImageDataStore
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationMetadata.Companion.STATION_METADATA_ENTITY_1
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationMetadata.Companion.STATION_METADATA_ENTITY_2
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MetadataStoreAndroidTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val database = Room.inMemoryDatabaseBuilder(
        context, CachingDatabase::class.java
    ).build()

    private val imageStore = ImageDataStore("${targetContext.dataDir}/image_test")

    private val stationStore = StationStore(
        database.getStationDao(),
        database.getBearerDao(),
        database.getStationImageDao(),
        database.getImageDao(),
        imageStore,
        database.getETagsDao()
    )
    private val metadataStore = MetadataStore(
        database.getStationMetadataDao(),
        database.getETagsDao()
    )

    @Test
    fun updateMetadata_shouldReplaceExistingMetadataForStation() = runBlocking {

        val stationId = stationStore.createStation("test_bearer")

        metadataStore.updateMetadata(stationId, STATION_METADATA_ENTITY_1)

        val metadataOne = database.getStationMetadataDao().findByStationId(stationId)!!

        Assert.assertEquals(STATION_METADATA_ENTITY_1.shortName, metadataOne.shortName)
        Assert.assertEquals(STATION_METADATA_ENTITY_1.mediumName, metadataOne.mediumName)
        Assert.assertEquals(STATION_METADATA_ENTITY_1.longName, metadataOne.longName)
        Assert.assertEquals(STATION_METADATA_ENTITY_1.description, metadataOne.description)

        metadataStore.updateMetadata(stationId, STATION_METADATA_ENTITY_2)

        val metadataTwo = database.getStationMetadataDao().findByStationId(stationId)!!

        Assert.assertEquals(STATION_METADATA_ENTITY_2.shortName, metadataTwo.shortName)
        Assert.assertEquals(STATION_METADATA_ENTITY_2.mediumName, metadataTwo.mediumName)
        Assert.assertEquals(STATION_METADATA_ENTITY_2.longName, metadataTwo.longName)
        Assert.assertEquals(STATION_METADATA_ENTITY_2.description, metadataTwo.description)

        Assert.assertEquals(metadataOne.metadataId, metadataTwo.metadataId)
        Assert.assertEquals(metadataOne.stationId, metadataTwo.stationId)
    }

    @After
    fun tearDown() {
        File("${targetContext.dataDir}/image_test").deleteRecursively()
    }
}

