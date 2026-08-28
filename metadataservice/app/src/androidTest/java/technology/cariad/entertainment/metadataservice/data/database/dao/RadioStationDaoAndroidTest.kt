package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.common.util.fromBearerString
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.datastore.RadioStationDataStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.BearerStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.GenreStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.ImageStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.LifeCycleDataStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.MetadataStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.ProviderIdentifierStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.StationStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.StreamStore
import technology.cariad.entertainment.metadataservice.data.filesystem.ImageDataStore
import technology.cariad.entertainment.metadataservice.data.provider.TimeProvider
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity.Companion.QualityType
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleLifeCycleData
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private val TEST_STATION = StationEntity(
    StationMetadataEntity(
        "cachedShortName",
        "cachedMediumName",
        "cachedLongName",
        "description",
        listOf("Rock", "Pop", "Metal", "Alternative", "HipHop"),
        ""
    ), ImageEntity(
        null, "logo.png", ImageEntity.Companion.ImageType.LOGO, null, ""
    ), ImageEntity(
        null,
        "smallLogo.png",
        ImageEntity.Companion.ImageType.LOGO_SMALL,
        null,""
    ), StationStreamListEntity(
        listOf(
            StationStreamEntity(
                "https://samplestream.com/stream.mp3", 0, QualityType.UNKNOWN
            ),
            StationStreamEntity(
                "https://samplestream.com/stream.aac", 10, QualityType.UNKNOWN
            )
        ), ""
    ), BearerListEntity(
        listOf(
            fromBearerString("hd:292.07426.1")!!,
            fromBearerString("hd:292.07427.1")!!
        ), ""
    ),
    listOf(
        ProviderIdentifierEntity(
            ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
            "gracenote_id"
        )
    ),
    SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
)

@RunWith(AndroidJUnit4::class)
class RadioStationDaoAndroidTest {
    private lateinit var db: CachingDatabase
    private lateinit var bearerDao: BearerDao
    private lateinit var stationDao: StationDao
    private lateinit var stationMetadataDao: StationMetadataDao
    private lateinit var genreDao: GenreDao
    private lateinit var stationGenreRankDao: StationGenreRankDao
    private lateinit var imageDao: ImageDao
    private lateinit var stationImageDao: StationImageDao
    private lateinit var streamDao: StreamDao
    private lateinit var lifeCycleDataDao: LifeCycleDataDao
    private lateinit var providerIdentifierDao: ProviderIdentifierDao
    private lateinit var stationProviderIdentifierDao: StationProviderIdentifierDao
    private lateinit var eTagsDao: ETagsDao
    private lateinit var bearerStore: BearerStore
    private lateinit var stationStore: StationStore
    private lateinit var metadataStore: MetadataStore
    private lateinit var genreStore: GenreStore
    private lateinit var imageStore: ImageStore
    private lateinit var streamStore: StreamStore
    private lateinit var providerIdentifierStore: ProviderIdentifierStore
    private lateinit var lifeCycleDataStore: LifeCycleDataStore
    private lateinit var radioStationDataStore: RadioStationDataStore
    private lateinit var imageDataStore: ImageDataStore
    private lateinit var timeProvider: TimeProvider

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = inMemoryDatabaseBuilder(
            context, CachingDatabase::class.java
        ).build()
        bearerDao = db.getBearerDao()
        stationDao = db.getStationDao()
        stationMetadataDao = db.getStationMetadataDao()
        genreDao = db.getGenreDao()
        stationGenreRankDao = db.getStationGenreRankDao()
        imageDao = db.getImageDao()
        stationImageDao = db.getStationImageDao()
        streamDao = db.getStreamDao()
        lifeCycleDataDao = db.getLifeCycleDataDao()
        providerIdentifierDao = db.getProviderIdentifierDao()
        stationProviderIdentifierDao = db.getStationProviderIdentifierDao()
        eTagsDao = db.getETagsDao()
        imageDataStore = ImageDataStore("${context.filesDir}/image_cache/gracenote")
        stationStore = StationStore(
            stationDao,
            bearerDao,
            stationImageDao,
            imageDao,
            imageDataStore,
            eTagsDao
        )
        bearerStore = BearerStore(bearerDao, eTagsDao)
        genreStore = GenreStore(genreDao, stationGenreRankDao)
        imageStore = ImageStore(stationImageDao, imageDao, eTagsDao)
        lifeCycleDataStore = LifeCycleDataStore(lifeCycleDataDao, stationDao)
        metadataStore = MetadataStore(stationMetadataDao, eTagsDao)
        providerIdentifierStore =
            ProviderIdentifierStore(providerIdentifierDao, stationProviderIdentifierDao)
        streamStore = StreamStore(streamDao, eTagsDao)
        timeProvider = TimeProvider()
        radioStationDataStore = RadioStationDataStore(
            db,
            bearerStore,
            stationStore,
            metadataStore,
            genreStore,
            imageStore,
            imageDataStore,
            streamStore,
            providerIdentifierStore,
            lifeCycleDataStore,
            timeProvider
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertRadioStation_shouldInsertOneStationAndUpdateLastRequested() = runBlocking {

        val bearer = fromBearerString("hd:292.07426.1")!!

        radioStationDataStore.updateBearers(bearer, TEST_STATION.bearerList!!)
        radioStationDataStore.updateMetadata(bearer, TEST_STATION.metadata!!)
        radioStationDataStore.updateStreams(bearer, TEST_STATION.streamList!!)
        radioStationDataStore.updateLogo(bearer, TEST_STATION.logo!!)
        radioStationDataStore.updateLogo(bearer, TEST_STATION.logoSmall!!)
        radioStationDataStore.updateProviderIdentifiers(bearer, TEST_STATION.providerIdentifiers)
        radioStationDataStore.updateLifeCycleData(bearer, TEST_STATION.lifeCycleData!!)

        val station = radioStationDataStore.getDataForBearer(bearer)

        Assert.assertEquals(TEST_STATION.logo, station?.logo)
        Assert.assertEquals(TEST_STATION.logoSmall, station?.logoSmall)
        Assert.assertEquals(TEST_STATION.bearerList, station?.bearerList)
        Assert.assertEquals(TEST_STATION.metadata, station?.metadata)
        Assert.assertEquals(TEST_STATION.streamList, station?.streamList)
        Assert.assertEquals(TEST_STATION.providerIdentifiers, station?.providerIdentifiers)

        Assert.assertNotEquals(TEST_STATION.lifeCycleData, station?.lifeCycleData)
    }
}

