package technology.cariad.entertainment.metadataservice.data.datastore

import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.AlbumStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.ArtistStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.CoverArtStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.ImageStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.LifeCycleStore
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.mock.android.TransactionMock
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleCoverArtLookup
import technology.cariad.entertainment.metadataservice.sampledata.SampleCoverArtRequestData
import technology.cariad.entertainment.metadataservice.sampledata.SampleImage
import technology.cariad.entertainment.metadataservice.sampledata.SampleLifeCycleData
import io.mockk.Runs
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val FAKE_IMAGE_ID = 10L
private const val FAKE_ALBUM_ID = 1L
private const val FAKE_ARTIST_ID = 4L
private const val FAKE_LIFE_CYCLE_DATA_ID = 6L
private const val FAKE_NUM_OF_COVER_ART_LOOKUP = 6
private const val FAKE_NUM_OF_COVER_ART_LOOKUP_2 = 1001
private const val FAKE_COVER_ART_LOOKUP_ID = 5L

class CoverArtDataStoreTest {
    private val imageType = ImageEntity.Companion.ImageType.COVER_ART
    private val fileName = SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL.coverArt!!.fileName

    private val databaseMock: CachingDatabase = mockk()
    private val coverArtStoreMock: CoverArtStore = mockk()
    private val albumStoreMock: AlbumStore = mockk()
    private val artistStoreMock: ArtistStore = mockk()
    private val imageStoreMock: ImageStore = mockk()
    private val imageDataStoreMock: IImageDataStore = mockk(relaxed = true)
    private val lifeCycleStoreMock: LifeCycleStore = mockk()

    private val coverArtDataStore = CoverArtDataStore(
        databaseMock,
        coverArtStoreMock,
        albumStoreMock,
        artistStoreMock,
        imageStoreMock,
        imageDataStoreMock,
        lifeCycleStoreMock
    )

    @Before
    fun setup() {
        LoggerMock()
        TransactionMock(databaseMock)
    }

    @Test
    fun getCoverArt_shouldReturnImageEntityIfAvailable() =
        runBlockingTest {
            coEvery { albumStoreMock.getAlbumIdByName(any()) } returns FAKE_ALBUM_ID
            coEvery { artistStoreMock.getArtistIdByName(any()) } returns FAKE_ARTIST_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookup(
                    any(),
                    any(),
                    any()
                )
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL
            every {
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
            } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
            coEvery { lifeCycleStoreMock.updateLifeCycleData(any()) } just Runs
            every { imageDataStoreMock.loadImageUri(any()) } returns "uri"

            val imageEntity = coverArtDataStore.getCoverArt(
                CoverArtRequestDataEntity(
                    artist = "Madonna",
                    album = "Like a Prayer",
                    title = "Cherish",
                    lifeCycleData = SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
                )
            )

            Assert.assertEquals(
                imageEntity,
                ImageEntity(
                    fileName = fileName,
                    uri = "uri",
                    imageData = null,
                    imageType = imageType
                )
            )
        }

    @Test
    fun getCoverArt_shouldReturnNoResultIfMissing() =
        runBlockingTest {
            coEvery { albumStoreMock.getAlbumIdByName(any()) } returns FAKE_ALBUM_ID
            coEvery { artistStoreMock.getArtistIdByName(any()) } returns null
            coEvery {
                coverArtStoreMock.getCoverArtLookup(
                    any(), any(), any()
                )
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_WITH_ONLY_ALBUM
            every {
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
            } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
            coEvery { lifeCycleStoreMock.updateLifeCycleData(any()) } just Runs
            every { imageDataStoreMock.loadImageUri(any()) } returns null

            val imageEntity = coverArtDataStore.getCoverArt(
                CoverArtRequestDataEntity(
                    artist = "",
                    album = "Like a Prayer",
                    title = "",
                    lifeCycleData = SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
                )
            )

            Assert.assertEquals(
                imageEntity,
                ImageEntity(fileName = fileName, imageData = null, imageType = imageType)
            )
        }

    @Test
    fun getCoverArt_shouldReturnNullIfCoverArtLookupNotFound() =
        runBlockingTest {
            coEvery {
                coverArtStoreMock.getCoverArtLookup(
                    any(),
                    any(),
                    any()
                )
            } returns null

            val imageEntity = coverArtDataStore.getCoverArt(
                CoverArtRequestDataEntity(
                    artist = null,
                    album = null,
                    title = "",
                    lifeCycleData = null
                )
            )

            Assert.assertEquals(null, imageEntity)
        }

    @Test
    fun getCoverArt_shouldReturnNullIfCoverArtLookupFoundButItsCoverArtIsNull() =
        runBlockingTest {
            coEvery { albumStoreMock.getAlbumIdByName(any()) } returns FAKE_ALBUM_ID
            coEvery { artistStoreMock.getArtistIdByName(any()) } returns FAKE_ARTIST_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookup(
                    any(),
                    any(),
                    any()
                )
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_POJO
            every {
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
            } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
            coEvery { lifeCycleStoreMock.updateLifeCycleData(any()) } just Runs

            val imageEntity = coverArtDataStore.getCoverArt(
                CoverArtRequestDataEntity(
                    artist = "Madonna",
                    album = "Like a Prayer",
                    title = "",
                    lifeCycleData = SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
                )
            )

            Assert.assertEquals(null, imageEntity)
        }

    @Test
    fun addCoverArtLookup_shouldDoNothingAndReturnNullIfLifeCycleDataMissing() = runBlockingTest {
        val uri = coverArtDataStore.addCoverArtLookup(
            SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_1, SampleImage.IMAGE_ENTITY_1
        )

        Assert.assertEquals(null, uri)

        verify {
            coverArtStoreMock wasNot called
            albumStoreMock wasNot called
            artistStoreMock wasNot called
            imageStoreMock wasNot called
            lifeCycleStoreMock wasNot called
        }
    }

    @Test
    fun addCoverArtLookup_shouldAddLookupAndReturnUriIfCacheNotFullAndUriIsValid() =
        runBlockingTest {
            coEvery {
                coverArtStoreMock.getCoverArtLookupCount()
            } returns FAKE_NUM_OF_COVER_ART_LOOKUP
            coEvery { albumStoreMock.selectOrInsertAlbum(any()) } returns FAKE_ALBUM_ID
            coEvery { artistStoreMock.selectOrInsertArtist(any()) } returns FAKE_ARTIST_ID
            coEvery { imageStoreMock.createImageEntry(any()) } returns SampleImage.IMAGE_ENTRY_1
            coEvery { imageStoreMock.insertImage(any()) } returns FAKE_IMAGE_ID
            every {
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
            } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
            coEvery {
                lifeCycleStoreMock.insertLifeCycleData(any())
            } returns FAKE_LIFE_CYCLE_DATA_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_ENTRY_1
            coEvery { coverArtStoreMock.insertCoverArtLookupEntry(any()) } returns 5L
            every { imageDataStoreMock.saveImage(any(), any()) } returns "uri"

            val uri = coverArtDataStore.addCoverArtLookup(
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_2,
                SampleImage.IMAGE_ENTITY_WITH_IMAGE_DATA
            )

            Assert.assertEquals("uri", uri)

            coVerifySequence {
                coverArtStoreMock.getCoverArtLookupCount()
                albumStoreMock.selectOrInsertAlbum(any())
                artistStoreMock.selectOrInsertArtist(any())
                imageStoreMock.createImageEntry(any())
                imageStoreMock.insertImage(any())
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
                lifeCycleStoreMock.insertLifeCycleData(any())
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
                coverArtStoreMock.insertCoverArtLookupEntry(any())
                imageDataStoreMock.saveImage(any(), any())
            }
        }

    @Test
    fun addCoverArtLookup_shouldNotInsertInDbAndShouldReturnNullIfCacheNotFullButUriNotValid() =
        runBlockingTest {
            coEvery {
                coverArtStoreMock.getCoverArtLookupCount()
            } returns FAKE_NUM_OF_COVER_ART_LOOKUP
            coEvery { albumStoreMock.selectOrInsertAlbum(any()) } returns FAKE_ALBUM_ID
            coEvery { artistStoreMock.selectOrInsertArtist(any()) } returns FAKE_ARTIST_ID
            coEvery { imageStoreMock.createImageEntry(any()) } returns SampleImage.IMAGE_ENTRY_1
            coEvery { imageStoreMock.insertImage(any()) } returns FAKE_IMAGE_ID
            every {
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
            } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
            coEvery {
                lifeCycleStoreMock.insertLifeCycleData(any())
            } returns FAKE_LIFE_CYCLE_DATA_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_ENTRY_1
            coEvery { coverArtStoreMock.insertCoverArtLookupEntry(any()) } returns 5L
            every { imageDataStoreMock.saveImage(any(), any()) } returns null

            val uri = coverArtDataStore.addCoverArtLookup(
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_2,
                SampleImage.IMAGE_ENTITY_WITH_IMAGE_DATA
            )

            Assert.assertEquals(null, uri)

            coVerifySequence {
                coverArtStoreMock.getCoverArtLookupCount()
                albumStoreMock.selectOrInsertAlbum(any())
                artistStoreMock.selectOrInsertArtist(any())
                imageStoreMock.createImageEntry(any())
                imageStoreMock.insertImage(any())
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
                lifeCycleStoreMock.insertLifeCycleData(any())
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
                coverArtStoreMock.insertCoverArtLookupEntry(any())
                imageDataStoreMock.saveImage(any(), any())
            }
        }

    @Test
    fun addCoverArtLookup_shouldNotInsertInDbAndReturnNullIfFailsFreeCacheBecauseOldestIdNotFound() =
        runBlockingTest {
            coEvery {
                coverArtStoreMock.getCoverArtLookupCount()
            } returns FAKE_NUM_OF_COVER_ART_LOOKUP_2
            coEvery { coverArtStoreMock.findLeastRecentlyUsedId() } returns null

            val uri = coverArtDataStore.addCoverArtLookup(
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_2,
                SampleImage.IMAGE_ENTITY_1
            )

            Assert.assertEquals(null, uri)

            coVerifySequence {
                coverArtStoreMock.getCoverArtLookupCount()
                coverArtStoreMock.findLeastRecentlyUsedId()
            }
        }

    @Test
    fun addCoverArtLookup_shouldNotInsertInDbAndReturnNullIfFailsFreeCacheBecauseCoverArtNotFound() =
        runBlockingTest {
            coEvery {
                coverArtStoreMock.getCoverArtLookupCount()
            } returns FAKE_NUM_OF_COVER_ART_LOOKUP_2
            coEvery { coverArtStoreMock.findLeastRecentlyUsedId() } returns FAKE_COVER_ART_LOOKUP_ID
            coEvery { coverArtStoreMock.getCoverArtLookupById(any()) } returns null

            val uri = coverArtDataStore.addCoverArtLookup(
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_2,
                SampleImage.IMAGE_ENTITY_1
            )

            Assert.assertEquals(null, uri)

            coVerifySequence {
                coverArtStoreMock.getCoverArtLookupCount()
                coverArtStoreMock.findLeastRecentlyUsedId()
                coverArtStoreMock.getCoverArtLookupById(any())
            }
        }

    @Test
    fun addCoverArtLookup_shouldFreeTheFullCacheAndInsertLookupAndReturnValidUri() =
        runBlockingTest {
            coEvery {
                coverArtStoreMock.getCoverArtLookupCount()
            } returns FAKE_NUM_OF_COVER_ART_LOOKUP_2
            coEvery { coverArtStoreMock.findLeastRecentlyUsedId() } returns FAKE_COVER_ART_LOOKUP_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookupById(any())
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL
            coEvery { coverArtStoreMock.deleteCoverArtById(any()) } returns 1
            every { imageDataStoreMock.deleteImage(any()) } returns true
            coEvery { albumStoreMock.selectOrInsertAlbum(any()) } returns FAKE_ALBUM_ID
            coEvery { artistStoreMock.selectOrInsertArtist(any()) } returns FAKE_ARTIST_ID
            coEvery { imageStoreMock.createImageEntry(any()) } returns SampleImage.IMAGE_ENTRY_1
            coEvery { imageStoreMock.insertImage(any()) } returns FAKE_IMAGE_ID
            every {
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
            } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
            coEvery {
                lifeCycleStoreMock.insertLifeCycleData(any())
            } returns FAKE_LIFE_CYCLE_DATA_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_ENTRY_1
            coEvery { coverArtStoreMock.insertCoverArtLookupEntry(any()) } returns 5L
            every { imageDataStoreMock.saveImage(any(), any()) } returns "uri"

            val uri = coverArtDataStore.addCoverArtLookup(
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_2,
                SampleImage.IMAGE_ENTITY_WITH_IMAGE_DATA
            )

            Assert.assertEquals("uri", uri)

            coVerifySequence {
                coverArtStoreMock.getCoverArtLookupCount()
                coverArtStoreMock.findLeastRecentlyUsedId()
                coverArtStoreMock.getCoverArtLookupById(any())
                coverArtStoreMock.deleteCoverArtById(any())
                imageDataStoreMock.deleteImage(any())
                albumStoreMock.selectOrInsertAlbum(any())
                artistStoreMock.selectOrInsertArtist(any())
                imageStoreMock.createImageEntry(any())
                imageStoreMock.insertImage(any())
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
                lifeCycleStoreMock.insertLifeCycleData(any())
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
                coverArtStoreMock.insertCoverArtLookupEntry(any())
                imageDataStoreMock.saveImage(any(), any())
            }
        }

    @Test
    fun addCoverArtLookup_shouldReturnNullIfSavingFileReturnsInvalidUri() =
        runBlockingTest {
            coEvery {
                coverArtStoreMock.getCoverArtLookupCount()
            } returns FAKE_NUM_OF_COVER_ART_LOOKUP_2
            coEvery { coverArtStoreMock.findLeastRecentlyUsedId() } returns FAKE_COVER_ART_LOOKUP_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookupById(any())
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL
            coEvery { coverArtStoreMock.deleteCoverArtById(any()) } returns 1
            every { imageDataStoreMock.deleteImage(any()) } returns true
            coEvery { albumStoreMock.selectOrInsertAlbum(any()) } returns FAKE_ALBUM_ID
            coEvery { artistStoreMock.selectOrInsertArtist(any()) } returns FAKE_ARTIST_ID
            coEvery { imageStoreMock.createImageEntry(any()) } returns SampleImage.IMAGE_ENTRY_1
            coEvery { imageStoreMock.insertImage(any()) } returns FAKE_IMAGE_ID
            every {
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
            } returns SampleLifeCycleData.LIFE_CYCLE_DATA_ENTRY_1
            coEvery {
                lifeCycleStoreMock.insertLifeCycleData(any())
            } returns FAKE_LIFE_CYCLE_DATA_ID
            coEvery {
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
            } returns SampleCoverArtLookup.COVER_ART_LOOKUP_ENTRY_1
            coEvery { coverArtStoreMock.insertCoverArtLookupEntry(any()) } returns 5L
            every { imageDataStoreMock.saveImage(any(), any()) } returns null

            val uri = coverArtDataStore.addCoverArtLookup(
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_2,
                SampleImage.IMAGE_ENTITY_WITH_IMAGE_DATA
            )

            Assert.assertEquals(null, uri)

            coVerifySequence {
                coverArtStoreMock.getCoverArtLookupCount()
                coverArtStoreMock.findLeastRecentlyUsedId()
                coverArtStoreMock.getCoverArtLookupById(any())
                coverArtStoreMock.deleteCoverArtById(any())
                imageDataStoreMock.deleteImage(any())
                albumStoreMock.selectOrInsertAlbum(any())
                artistStoreMock.selectOrInsertArtist(any())
                imageStoreMock.createImageEntry(any())
                imageStoreMock.insertImage(any())
                lifeCycleStoreMock.createLifeCycleDataEntry(any(), any())
                lifeCycleStoreMock.insertLifeCycleData(any())
                coverArtStoreMock.getCoverArtLookupEntry(any(), any(), any(), any(), any())
                coverArtStoreMock.insertCoverArtLookupEntry(any())
                imageDataStoreMock.saveImage(any(), any())
            }
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

