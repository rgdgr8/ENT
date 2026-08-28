package technology.cariad.entertainment.metadataservice.data.datastore

import de.esolutions.media.metadataservice.sampledata.SampleStream.Companion.STREAM_ENTRY_1
import de.esolutions.media.metadataservice.sampledata.SampleStream.Companion.STREAM_ENTRY_2
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ETagsEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationMetadataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry
import technology.cariad.entertainment.metadataservice.data.database.pojo.CompleteRadioStation
import technology.cariad.entertainment.metadataservice.data.database.sampledata.SampleBearer
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
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity
import technology.cariad.entertainment.metadataservice.mock.android.TransactionMock
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleLifeCycleData
import technology.cariad.entertainment.metadataservice.sampledata.SampleProviderIdentifier
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationLogo
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationMetadata.Companion.STATION_METADATA_ENTRY_1
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationStream.Companion.STATION_STREAM_ENTITY_1
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationStream.Companion.STATION_STREAM_ENTITY_2
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

private const val STATION_ID = 1L
private const val IMAGE_ID = 4L
private const val INVALID_DATA_INDEX = -1L

class RadioStationDataStoreTest {

    private val database: CachingDatabase = mockk(relaxed = true)
    private val bearerStore: BearerStore = mockk(relaxed = true)
    private val stationStore: StationStore = mockk(relaxed = true)
    private val metadataStore: MetadataStore = mockk(relaxed = true)
    private val genreStore: GenreStore = mockk(relaxed = true)
    private val imageStore: ImageStore = mockk(relaxed = true)
    private val imageDataStore: ImageDataStore = mockk(relaxed = true)
    private val streamStore: StreamStore = mockk(relaxed = true)
    private val providerIdentifierStore: ProviderIdentifierStore = mockk(relaxed = true)
    private val lifeCycleDataStore: LifeCycleDataStore = mockk(relaxed = true)
    private val timeProvider: TimeProvider = mockk(relaxed = true)

    private lateinit var dataStore: RadioStationDataStore

    @Before
    fun setup() {

        LoggerMock()
        TransactionMock(database)

        dataStore = RadioStationDataStore(
            database,
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

    @Test
    fun updateBearers_shouldUpdateBearersIfStationValid() = runBlockingTest {

        val bearers = BearerListEntity(
            listOf(
                SampleBearer.FM_BEARER_ENTITY_1,
                SampleBearer.DAB_BEARER_ENTITY_1,
                SampleBearer.IBOC_BEARER_ENTITY_1
            ), "etag"
        )

        coEvery {
            stationStore.getOrCreateStation(any())
        } returns STATION_ID
        coEvery { bearerStore.updateBearers(any(), any()) } returns Unit

        dataStore.updateBearers(SampleBearer.FM_BEARER_ENTITY_1, bearers)

        coVerify {
            stationStore.getOrCreateStation(SampleBearer.FM_BEARER_ENTITY_1)
            bearerStore.updateBearers(STATION_ID, bearers)
        }
    }

    @Test
    fun updateBearers_shouldDoNothingIfStationNotValid() = runBlockingTest {

        val bearers = BearerListEntity(
            listOf(
                SampleBearer.FM_BEARER_ENTITY_1,
                SampleBearer.DAB_BEARER_ENTITY_1,
                SampleBearer.IBOC_BEARER_ENTITY_1
            ), "etag"
        )

        coEvery { stationStore.getOrCreateStation(any()) } returns INVALID_DATA_INDEX

        dataStore.updateBearers(SampleBearer.FM_BEARER_ENTITY_1, bearers)

        coVerify { stationStore.getOrCreateStation(SampleBearer.FM_BEARER_ENTITY_1) }
        coVerify(inverse = true) { bearerStore.updateBearers(STATION_ID, bearers) }
    }

    @Test
    fun updateMetadata_shouldUpdateStationMetadataIfStationValid() = runBlockingTest {

        val stationMetaData =
            StationMetadataEntity(
                shortName = "Short Name",
                mediumName = "Medium Name",
                longName = "Long Name",
                description = "Description",
                genres = listOf("Rock", "Metal"),
                etag = "etag"
            )

        coEvery { stationStore.getOrCreateStation(any()) } returns STATION_ID
        coEvery { metadataStore.updateMetadata(STATION_ID, stationMetaData) } returns Unit
        coEvery { genreStore.updateGenres(any(), any()) } returns Unit

        dataStore.updateMetadata(SampleBearer.FM_BEARER_ENTITY_1, stationMetaData)

        coVerify {
            stationStore.getOrCreateStation(SampleBearer.FM_BEARER_ENTITY_1)
            metadataStore.updateMetadata(STATION_ID, stationMetaData)
            genreStore.updateGenres(STATION_ID, listOf("Rock", "Metal"))
        }
    }

    @Test
    fun updateMetadata_shouldRemoveGenresIfMissingAndStationValid() =
        runBlockingTest {

            val stationMetaData =
                StationMetadataEntity(
                    shortName = "Short Name",
                    mediumName = "Medium Name",
                    longName = "Long Name",
                    description = "Description",
                    genres = null,
                    etag = "etag"
                )

            coEvery { stationStore.getOrCreateStation(any()) } returns STATION_ID
            coEvery { metadataStore.updateMetadata(STATION_ID, stationMetaData) } returns Unit
            coEvery { genreStore.updateGenres(any(), any()) } returns Unit

            dataStore.updateMetadata(SampleBearer.FM_BEARER_ENTITY_1, stationMetaData)

            coVerify {
                stationStore.getOrCreateStation(SampleBearer.FM_BEARER_ENTITY_1)
                metadataStore.updateMetadata(STATION_ID, stationMetaData)
                genreStore.updateGenres(STATION_ID, listOf())
            }
        }

    @Test
    fun updateMetadata_shouldDoNothingIfStationNotValid() = runBlockingTest {

        val stationMetaData =
            StationMetadataEntity(
                null, null, null, null, listOf(), null
            )

        coEvery { stationStore.getOrCreateStation(any()) } returns INVALID_DATA_INDEX

        dataStore.updateMetadata(SampleBearer.FM_BEARER_ENTITY_1, stationMetaData)

        coVerify { stationStore.getOrCreateStation(SampleBearer.FM_BEARER_ENTITY_1) }
        coVerify(inverse = true) { metadataStore.updateMetadata(STATION_ID, stationMetaData) }
    }

    @Test
    fun updateLifeCycleData_shouldUpdateLifeCycleDataIfStationValid() = runBlockingTest {

        coEvery { stationStore.getOrCreateStation(any()) } returns STATION_ID
        coEvery { lifeCycleDataStore.updateLifeCycleData(any(), any()) } returns Unit

        dataStore.updateLifeCycleData(
            SampleBearer.FM_BEARER_ENTITY_1,
            SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
        )

        coVerify {
            stationStore.getOrCreateStation(SampleBearer.FM_BEARER_ENTITY_1)
            lifeCycleDataStore.updateLifeCycleData(
                STATION_ID,
                SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
            )
        }
    }

    @Test
    fun updateLifeCycleData_shouldDoNothingIfStationNotValid() =
        runBlockingTest {

            coEvery { stationStore.getOrCreateStation(any()) } returns INVALID_DATA_INDEX

            dataStore.updateLifeCycleData(
                SampleBearer.FM_BEARER_ENTITY_1,
                SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
            )

            coVerify { stationStore.getOrCreateStation(SampleBearer.FM_BEARER_ENTITY_1) }
            coVerify(inverse = true) {
                lifeCycleDataStore.updateLifeCycleData(
                    STATION_ID,
                    SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
                )
            }
        }

    @Test
    fun updateLogo_shouldUpdateAndReturnLogoIfStationValid() = runBlockingTest {

        coEvery { stationStore.getOrCreateStation(any()) } returns STATION_ID
        every { imageDataStore.saveImage(any(), any()) } returns "uri"
        coEvery { imageStore.updateLogo(any(), any()) } returns Unit

        val logo = dataStore.updateLogo(
            SampleBearer.DAB_BEARER_ENTITY_1,
            SampleStationLogo.IMAGE_ENTITY_1
        )

        Assert.assertEquals(SampleStationLogo.STATION_LOGO_WITH_INVALID_IMAGE_DATA, logo)

        coVerify {
            stationStore.getOrCreateStation(SampleBearer.DAB_BEARER_ENTITY_1)
            imageStore.updateLogo(STATION_ID, any())
        }
        verify { imageDataStore.saveImage(any(), any()) }
    }

    @Test
    fun updateLogo_shouldDoNothingAndJustReturnTheSameLogoIfStationNotValid() =
        runBlockingTest {

            coEvery { stationStore.getOrCreateStation(any()) } returns INVALID_DATA_INDEX

            val logo = dataStore.updateLogo(
                SampleBearer.DAB_BEARER_ENTITY_1,
                SampleStationLogo.IMAGE_ENTITY_1
            )

            Assert.assertEquals(SampleStationLogo.IMAGE_ENTITY_1, logo)

            coVerify { stationStore.getOrCreateStation(SampleBearer.DAB_BEARER_ENTITY_1) }
            coVerify(inverse = true) {
                imageStore.updateLogo(
                    STATION_ID,
                    SampleStationLogo.IMAGE_ENTITY_1
                )
            }
        }

    @Test
    fun updateLogo_shouldUpdateLogoWithCachedUriAndReturnItIfStationValidButImageDataNull() =
        runBlockingTest {

            coEvery { stationStore.getOrCreateStation(any()) } returns STATION_ID
            every { imageDataStore.loadImageUri(any()) } returns "uri"
            coEvery { imageStore.updateLogo(any(), any()) } returns Unit

            val logo = dataStore.updateLogo(
                SampleBearer.DAB_BEARER_ENTITY_1,
                SampleStationLogo.IMAGE_ENTITY
            )

            Assert.assertEquals(SampleStationLogo.STATION_LOGO_WITH_INVALID_IMAGE_DATA, logo)

            coVerify { stationStore.getOrCreateStation(SampleBearer.DAB_BEARER_ENTITY_1) }
            verify { imageDataStore.loadImageUri("file_name") }
            coVerify {
                imageStore.updateLogo(
                    STATION_ID,
                    SampleStationLogo.STATION_LOGO_WITH_INVALID_IMAGE_DATA
                )
            }
        }

    @Test
    fun updateStreams_shouldUpdateStreamsIfStationValid() = runBlockingTest {

        val stationStreams = StationStreamListEntity(
            listOf(
                StationStreamEntity(
                    "", null, StationStreamEntity.Companion.QualityType.HIGH
                ),
                StationStreamEntity(
                    "", null, StationStreamEntity.Companion.QualityType.LOW
                )
            ), "etag"
        )

        coEvery { stationStore.getOrCreateStation(any()) } returns STATION_ID
        coEvery { streamStore.updateStreams(any(), any()) } returns Unit

        dataStore.updateStreams(
            SampleBearer.DAB_BEARER_ENTITY_1,
            stationStreams
        )

        coVerify {
            stationStore.getOrCreateStation(SampleBearer.DAB_BEARER_ENTITY_1)
            streamStore.updateStreams(STATION_ID, stationStreams)
        }
    }

    @Test
    fun updateStreams_shouldDoNothingIfStationNotValid() =
        runBlockingTest {

            val stationStreams = StationStreamListEntity(
                listOf(
                    StationStreamEntity(
                        "", null, StationStreamEntity.Companion.QualityType.HIGH
                    ),
                    StationStreamEntity(
                        "", null, StationStreamEntity.Companion.QualityType.LOW
                    )
                ), "etag"
            )

            coEvery { stationStore.getOrCreateStation(any()) } returns INVALID_DATA_INDEX

            dataStore.updateStreams(
                SampleBearer.DAB_BEARER_ENTITY_1,
                stationStreams
            )

            coVerify { stationStore.getOrCreateStation(SampleBearer.DAB_BEARER_ENTITY_1) }
            coVerify(inverse = true) { streamStore.updateStreams(STATION_ID, stationStreams) }
        }

    @Test
    fun updateProviderIdentifiers_shouldUpdateProviderIdentifiersIfStationValid() =
        runBlockingTest {

            val providerIdentifiers = listOf(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1,
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_1
            )

            coEvery { stationStore.getOrCreateStation(any()) } returns STATION_ID
            coEvery { providerIdentifierStore.updateProviderIdentifiers(any(), any()) } returns Unit

            dataStore.updateProviderIdentifiers(
                SampleBearer.DAB_BEARER_ENTITY_1,
                providerIdentifiers
            )

            coVerify {
                stationStore.getOrCreateStation(SampleBearer.DAB_BEARER_ENTITY_1)
                providerIdentifierStore.updateProviderIdentifiers(STATION_ID, providerIdentifiers)
            }
        }

    @Test
    fun updateProviderIdentifiers_shouldDoNothingIfStationNotValid() =
        runBlockingTest {

            val providerIdentifiers = listOf(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1,
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_1
            )

            coEvery { stationStore.getOrCreateStation(any()) } returns INVALID_DATA_INDEX

            dataStore.updateProviderIdentifiers(
                SampleBearer.DAB_BEARER_ENTITY_1,
                providerIdentifiers
            )

            coVerify { stationStore.getOrCreateStation(any()) }
            coVerify(inverse = true) {
                providerIdentifierStore.updateProviderIdentifiers(
                    STATION_ID,
                    providerIdentifiers
                )
            }
        }

    @Test
    fun deleteMetadata_shouldDeleteMetadataIfStationFound() = runBlockingTest {

        val bearer = SampleBearer.DAB_BEARER_ENTITY_2

        coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
        coEvery { metadataStore.deleteByStationId(any()) } returns Unit

        dataStore.deleteMetadata(bearer)

        coVerify {
            bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_2)
            metadataStore.deleteByStationId(STATION_ID)
        }
    }

    @Test
    fun deleteMetadata_shouldDoNothingIfStationNotFound() =
        runBlockingTest {

            val bearer = SampleBearer.DAB_BEARER_ENTITY_2

            coEvery { bearerStore.getStationIdByBearer(any()) } returns null

            dataStore.deleteMetadata(bearer)

            coVerify { bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_2) }
            coVerify(inverse = true) {
                metadataStore.deleteByStationId(STATION_ID)
                genreStore.deleteByStationId(STATION_ID)
            }
        }

    @Test
    fun deleteLogo_shouldDeleteLogoIfStationAndImageFoundAndDeletingFileItselfWasSuccessful() =
        runBlockingTest {

            val imageType = ImageEntity.Companion.ImageType.LOGO
            val stationImageEntry = StationImageEntry(imageId = IMAGE_ID, stationId = STATION_ID)

            coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
            coEvery { imageStore.findLogoByStationIdAndType(any(), any()) } returns IMAGE_ID
            every { imageStore.createStationImageEntry(any(), any()) } returns stationImageEntry
            coEvery { imageStore.delete(any(), imageType) } returns Unit
            every { imageDataStore.deleteImage(any()) } returns true

            dataStore.deleteLogo(SampleBearer.DAB_BEARER_ENTITY_1, imageType)

            coVerify {
                bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_1)
                imageStore.findLogoByStationIdAndType(STATION_ID, imageType)
                imageStore.delete(stationImageEntry, imageType)
            }
            verify {
                imageStore.createStationImageEntry(IMAGE_ID, STATION_ID)
                imageDataStore.deleteImage(any())
            }
        }

    @Test
    fun deleteLogo_shouldDeleteLogoIfStationAndImageFoundButDeletingFileItselfWasNotSuccessful() =
        runBlockingTest {

            val imageType = ImageEntity.Companion.ImageType.LOGO
            val stationImageEntry = StationImageEntry(imageId = IMAGE_ID, stationId = STATION_ID)

            coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
            coEvery { imageStore.findLogoByStationIdAndType(any(), any()) } returns IMAGE_ID
            every { imageStore.createStationImageEntry(any(), any()) } returns stationImageEntry
            coEvery { imageStore.delete(any(), any()) } returns Unit
            every { imageDataStore.deleteImage(any()) } returns false

            dataStore.deleteLogo(SampleBearer.DAB_BEARER_ENTITY_1, imageType)

            coVerify {
                bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_1)
                imageStore.findLogoByStationIdAndType(STATION_ID, imageType)
                imageStore.delete(stationImageEntry, imageType)
            }
            verify {
                imageStore.createStationImageEntry(IMAGE_ID, STATION_ID)
                imageDataStore.deleteImage(any())
            }
        }

    @Test
    fun deleteLogo_shouldDoNothingIfStationNotFound() = runBlockingTest {

        val imageType = ImageEntity.Companion.ImageType.LOGO

        coEvery { bearerStore.getStationIdByBearer(any()) } returns null

        dataStore.deleteLogo(SampleBearer.DAB_BEARER_ENTITY_1, imageType)

        coVerify { bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_1) }
        coVerify(inverse = true) {
            imageStore.findLogoByStationIdAndType(
                STATION_ID, imageType
            )
            imageStore.delete(any(), any())
        }
        verify(inverse = true) { imageStore.createStationImageEntry(any(), any()) }
    }

    @Test
    fun deleteLogo_shouldDoNothingIfLogoNotFound() = runBlockingTest {

        val imageType = ImageEntity.Companion.ImageType.LOGO

        coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
        coEvery { imageStore.findLogoByStationIdAndType(any(), any()) } returns null

        dataStore.deleteLogo(SampleBearer.DAB_BEARER_ENTITY_1, imageType)

        coVerify {
            bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_1)
            imageStore.findLogoByStationIdAndType(STATION_ID, imageType)
        }
        verify(inverse = true) { imageStore.createStationImageEntry(any(), any()) }
        coVerify(inverse = true) {
            imageStore.delete(any(), imageType)
        }
    }

    @Test
    fun deleteStream_shouldDeleteStationStreamsIfStationFound() =
        runBlockingTest {

            coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
            coEvery { streamStore.deleteByStationId(any()) } returns 2

            dataStore.deleteStreams(SampleBearer.DAB_BEARER_ENTITY_2)

            coVerify {
                bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_2)
                streamStore.deleteByStationId(STATION_ID)
            }
        }

    @Test
    fun deleteStream_shouldDoNothingIfStationNotFound() =
        runBlockingTest {

            coEvery { bearerStore.getStationIdByBearer(any()) } returns null

            dataStore.deleteStreams(SampleBearer.DAB_BEARER_ENTITY_2)

            coVerify { bearerStore.getStationIdByBearer(SampleBearer.DAB_BEARER_ENTITY_2) }
            coVerify(inverse = true) { streamStore.deleteByStationId(any()) }
        }

    @Test
    fun getDataForBearer_shouldReturnNullIfBearerInvalid() = runBlockingTest {

        val bearer = SampleBearer.FK_BEARER_ENTITY_1

        val stationEntity = dataStore.getDataForBearer(bearer)

        Assert.assertEquals(null, stationEntity)
    }

    @Test
    fun getDataForBearer_shouldReturnNullIfStationNotFound() = runBlockingTest {

        val bearer = SampleBearer.FM_BEARER_ENTITY_1

        coEvery { bearerStore.getStationIdByBearer(bearer) } returns null

        val stationEntity = dataStore.getDataForBearer(bearer)

        Assert.assertEquals(null, stationEntity)

        coVerify { bearerStore.getStationIdByBearer(bearer) }
    }

    @Test
    fun getDataForBearer_shouldReturnNullIfCompleteStationNotFound() = runBlockingTest {

        val bearer = SampleBearer.FM_BEARER_ENTITY_1

        coEvery { bearerStore.getStationIdByBearer(bearer) } returns STATION_ID
        coEvery { stationStore.getCompleteById(any()) } returns null

        val stationEntity = dataStore.getDataForBearer(bearer)

        Assert.assertEquals(null, stationEntity)

        coVerify {
            bearerStore.getStationIdByBearer(bearer)
            stationStore.getCompleteById(STATION_ID)
        }
    }

    @Test
    fun getDataForBearer_shouldReturnStationWithOrderedStreams() = runBlockingTest {
        val streamsUnordered = listOf(STREAM_ENTRY_2, STREAM_ENTRY_1)

        val completeRadioStation = CompleteRadioStation(
            providerIdentifiers = listOf(),
            genres = listOf(),
            bearers = listOf(),
            genreRanks = listOf(),
            images = listOf(),
            lifeCycleData = LifeCycleDataEntry(lastRequested = 0),
            station = StationEntry(0, 0),
            stationMetadata = null,
            streams = streamsUnordered,
            etags = ETagsEntry(0),
        )
        val orderedStreams = StationStreamListEntity(
            listOf(
                STATION_STREAM_ENTITY_1,
                STATION_STREAM_ENTITY_2
            ), null
        )

        coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
        coEvery { stationStore.getCompleteById(any()) } returns completeRadioStation

        val station = dataStore.getDataForBearer(SampleBearer.FM_BEARER_ENTITY_1)

        Assert.assertEquals(orderedStreams, station!!.streamList)
    }

    @Test
    fun getDataForBearer_shouldReturnStationWithMetaDataIfMetaDataNotNull() = runBlockingTest {

        val stationMetaData = STATION_METADATA_ENTRY_1
        val stationMetaDataEntity = StationMetadataEntity(
            shortName = stationMetaData.shortName,
            mediumName = stationMetaData.mediumName,
            longName = stationMetaData.longName,
            description = stationMetaData.description,
            genres = listOf(),
            etag = null
        )
        val completeRadioStation = CompleteRadioStation(
            providerIdentifiers = listOf(),
            genres = listOf(),
            bearers = listOf(),
            genreRanks = listOf(),
            images = listOf(),
            lifeCycleData = LifeCycleDataEntry(lastRequested = 0),
            station = StationEntry(0, 0),
            stationMetadata = stationMetaData,
            streams = listOf(),
            etags = ETagsEntry(0),
        )

        coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
        coEvery { stationStore.getCompleteById(any()) } returns completeRadioStation

        val station = dataStore.getDataForBearer(SampleBearer.FM_BEARER_ENTITY_1)

        Assert.assertEquals(stationMetaDataEntity, station!!.metadata)
    }

    @Test
    fun getDataForBearer_shouldReturnStationWithSortedGenresIfGenresNotNull() = runBlockingTest {

        val genres = listOf(
            GenreEntry(5, "Rock"),
            GenreEntry(3, "Metal"),
            GenreEntry(8, "Jazz"),
        )
        val genreRanks = listOf(
            StationGenreRankEntry(STATION_ID, 5, 2),
            StationGenreRankEntry(STATION_ID, 3, 0),
            StationGenreRankEntry(STATION_ID, 8, 1),
        )
        val stationMetaData = StationMetadataEntry(
            metadataId = 1L,
            STATION_ID,
            shortName = "Short Name",
            mediumName = "Medium Name",
            longName = "Long Name",
            description = "Description"
        )
        val stationMetaDataEntity = StationMetadataEntity(
            shortName = stationMetaData.shortName,
            mediumName = stationMetaData.mediumName,
            longName = stationMetaData.longName,
            description = stationMetaData.description,
            genres = listOf("Metal", "Jazz", "Rock"),
            etag = "etag"
        )
        val completeRadioStation = CompleteRadioStation(
            providerIdentifiers = listOf(),
            genres = genres,
            bearers = listOf(),
            genreRanks = genreRanks,
            images = listOf(),
            lifeCycleData = LifeCycleDataEntry(lastRequested = 0),
            station = StationEntry(0, 0),
            stationMetadata = stationMetaData,
            streams = listOf(),
            etags = ETagsEntry(0)
        )

        coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
        coEvery { stationStore.getCompleteById(any()) } returns completeRadioStation

        val station = dataStore.getDataForBearer(SampleBearer.FM_BEARER_ENTITY_1)

        Assert.assertEquals(stationMetaDataEntity.genres, station!!.metadata!!.genres)
    }

    // seperate test cases
    @Test
    fun getDataForBearer_shouldReturnStationWithLogoAndLogoSmallIfImagesNotNull() =
        runBlockingTest {

            val images = listOf(
                ImageEntry(
                    imageId = 0L,
                    imageType = ImageEntity.Companion.ImageType.LOGO.value,
                    fileName = "logoName"
                ),
                ImageEntry(
                    imageId = 0L,
                    imageType = ImageEntity.Companion.ImageType.LOGO_SMALL.value,
                    fileName = "logoSmallName"
                )
            )
            val logo = ImageEntity(
                uri = "uri00",
                fileName = images[0].fileName,
                imageType = ImageEntity.Companion.ImageType.LOGO,
                imageData = null
            )
            val logoSmall = ImageEntity(
                uri = "uri01",
                fileName = images[1].fileName,
                imageType = ImageEntity.Companion.ImageType.LOGO_SMALL,
                imageData = null
            )

            val completeRadioStation = CompleteRadioStation(
                providerIdentifiers = listOf(),
                genres = listOf(),
                bearers = listOf(),
                genreRanks = listOf(),
                images = images,
                lifeCycleData = LifeCycleDataEntry(lastRequested = 0),
                station = StationEntry(0, 0),
                stationMetadata = null,
                streams = listOf(),
                etags = ETagsEntry(0)
            )

            coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
            coEvery { stationStore.getCompleteById(any()) } returns completeRadioStation
            every { imageDataStore.loadImageUri("logoName") } returns logo.uri
            every { imageDataStore.loadImageUri("logoSmallName") } returns logoSmall.uri

            val station = dataStore.getDataForBearer(SampleBearer.FM_BEARER_ENTITY_1)

            Assert.assertEquals(logo, station?.logo)
            Assert.assertEquals(logoSmall, station?.logoSmall)
        }

    @Test
    fun getDataForBearer_shouldReturnStationWithAllInformation() =
        runBlockingTest {

            val genres = listOf(
                GenreEntry(5L, "Rock"),
                GenreEntry(3L, "Metal"),
                GenreEntry(8L, "Jazz"),
                GenreEntry(7L, "Country")
            )
            val genreRanks = listOf(
                StationGenreRankEntry(STATION_ID, 3, 0),
                StationGenreRankEntry(STATION_ID, 8, 1),
                StationGenreRankEntry(STATION_ID, 5, 2),
            )
            val bearers = listOf<BearerEntry>(
                BearerEntry(3, STATION_ID, SampleBearer.FM_BEARER_STRING_1),
                BearerEntry(1, STATION_ID, SampleBearer.FM_BEARER_STRING_2),
                BearerEntry(2, STATION_ID, SampleBearer.FM_BEARER_STRING_3),
                BearerEntry(5, STATION_ID, SampleBearer.FK_BEARER_STRING_1)
            )
            val images = listOf(
                ImageEntry(
                    imageId = 0L,
                    imageType = ImageEntity.Companion.ImageType.LOGO.value,
                    fileName = "logoName"
                ),
                ImageEntry(
                    imageId = 0L,
                    imageType = ImageEntity.Companion.ImageType.LOGO_SMALL.value,
                    fileName = "logoSmallName"
                )
            )
            val logo = ImageEntity(
                uri = "uri00",
                fileName = images[0].fileName,
                imageType = ImageEntity.Companion.ImageType.LOGO,
                imageData = null
            )
            val logoSmall = ImageEntity(
                uri = "uri01",
                fileName = images[1].fileName,
                imageType = ImageEntity.Companion.ImageType.LOGO_SMALL,
                imageData = null
            )
            val stationMetaData = StationMetadataEntry(
                metadataId = 1L,
                STATION_ID,
                shortName = "Short Name",
                mediumName = "Medium Name",
                longName = "Long Name",
                description = "Description"
            )
            val stationMetaDataEntity = StationMetadataEntity(
                shortName = stationMetaData.shortName,
                mediumName = stationMetaData.mediumName,
                longName = stationMetaData.longName,
                description = stationMetaData.description,
                genres = listOf("Metal", "Jazz", "Rock"),
                etag = null
            )

            val providerIdentifiers = listOf(
                ProviderIdentifierEntry(
                    providerId = 0L,
                    origin = ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                    externalId = "AppleMusic"

                ),
                ProviderIdentifierEntry(
                    providerId = 1L,
                    origin = ProviderIdentifierEntity.Companion.OriginType.RADIODNS,
                    externalId = "AmazonMusic"

                )
            )
            val streamsUnordered = listOf(
                StreamEntry(
                    1,
                    STATION_ID,
                    1,
                    "second_stream",
                    null,
                    StationStreamEntity.Companion.QualityType.LOW
                ),
                StreamEntry(
                    0,
                    STATION_ID,
                    0,
                    "first_stream",
                    null,
                    StationStreamEntity.Companion.QualityType.HIGH
                )
            )
            val completeRadioStation = CompleteRadioStation(
                providerIdentifiers = providerIdentifiers,
                genres = genres,
                bearers = bearers,
                genreRanks = genreRanks,
                images = images,
                lifeCycleData = LifeCycleDataEntry(lastRequested = 0),
                station = StationEntry(0, 0),
                stationMetadata = stationMetaData,
                streams = streamsUnordered,
                etags = ETagsEntry(0)
            )
            val orderedStreams = StationStreamListEntity(
                listOf(
                    StationStreamEntity(
                        "first_stream",
                        null,
                        StationStreamEntity.Companion.QualityType.HIGH
                    ),
                    StationStreamEntity(
                        "second_stream",
                        null,
                        StationStreamEntity.Companion.QualityType.LOW
                    )
                ),
                null
            )

            coEvery { bearerStore.getStationIdByBearer(any()) } returns STATION_ID
            coEvery { stationStore.getCompleteById(any()) } returns completeRadioStation
            every { imageDataStore.loadImageUri("logoName") } returns logo.uri
            every { imageDataStore.loadImageUri("logoSmallName") } returns logoSmall.uri

            val station = dataStore.getDataForBearer(SampleBearer.FM_BEARER_ENTITY_1)

            Assert.assertEquals(orderedStreams, station!!.streamList)
            Assert.assertEquals(stationMetaDataEntity, station.metadata)
            Assert.assertEquals(stationMetaDataEntity.genres, station.metadata?.genres)
            Assert.assertEquals(logo, station.logo)
            Assert.assertEquals(logoSmall, station.logoSmall)
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

