package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.CoverArtLookupDao
import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleCoverArtLookup
import technology.cariad.entertainment.metadataservice.sampledata.SampleCoverArtRequestData
import io.mockk.called
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

class CoverArtStoreTest {

    private val coverArtLookupDaoMock: CoverArtLookupDao = mockk()

    private val coverArtStore = CoverArtStore(coverArtLookupDaoMock)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun getCoverArtLookupCount_shouldReturnLookupsCount() = runBlockingTest {
        coEvery { coverArtLookupDaoMock.getCount() } returns 10

        val coverArtLookupCount = coverArtStore.getCoverArtLookupCount()

        Assert.assertEquals(10, coverArtLookupCount)
    }

    @Test
    fun findLeastRecentlyUsedId_shouldForwardNullableResultFromDao() = runBlockingTest {
        coEvery { coverArtLookupDaoMock.findLeastRecentlyUsedId() } returns null andThen 1L

        val nullResult = coverArtStore.findLeastRecentlyUsedId()
        val result = coverArtStore.findLeastRecentlyUsedId()

        Assert.assertNull(nullResult)
        Assert.assertEquals(1L, result)
    }

    @Test
    fun getCoverArtLookupById_shouldForwardNullableResultFromDao() = runBlockingTest {
        coEvery {
            coverArtLookupDaoMock.findById(any())
        } returns null andThen SampleCoverArtLookup.COVER_ART_LOOKUP_POJO

        val nullResult = coverArtStore.getCoverArtLookupById(10L)
        val result = coverArtStore.getCoverArtLookupById(10L)

        Assert.assertNull(nullResult)
        Assert.assertEquals(SampleCoverArtLookup.COVER_ART_LOOKUP_POJO, result)
    }

    @Test
    fun deleteCoverArtById_shouldForwardResultFromDao() = runBlockingTest {
        coEvery {
            coverArtLookupDaoMock.deleteById(any())
        } returns 1

        val deleteCount = coverArtStore.deleteCoverArtById(1L)

        Assert.assertEquals(1, deleteCount)
    }

    @Test
    fun getCoverArtLookupEntry_shouldForwardResultFromDao() = runBlockingTest {
        every {
            coverArtLookupDaoMock.createCoverArtLookupEntry(
                any(), any(), any(), any(), any()
            )
        } returns SampleCoverArtLookup.COVER_ART_LOOKUP_ENTRY_1

        val foundEntry = coverArtStore.getCoverArtLookupEntry(
            albumId = 1,
            artistId = 8,
            imageId = 10,
            lifeCycleDataId = 20,
            coverArtRequestData = SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_1
        )

        Assert.assertEquals(SampleCoverArtLookup.COVER_ART_LOOKUP_ENTRY_1, foundEntry)
    }

    @Test
    fun insertCoverArtLookupEntry_shouldForwardResultFromDao() = runBlockingTest {
        coEvery { coverArtLookupDaoMock.insert(any<CoverArtLookupEntry>()) } returns 5L

        val lookupId =
            coverArtStore.insertCoverArtLookupEntry(SampleCoverArtLookup.COVER_ART_LOOKUP_ENTRY_1)

        Assert.assertEquals(5L, lookupId)
    }

    @Test
    fun getCoverArtLookup_shouldReturnNullIfArgumentsAreInsufficient() = runBlockingTest {
        val allNull = coverArtStore.getCoverArtLookup(null, null, null)
        Assert.assertNull(allNull)

        val onlyArtist = coverArtStore.getCoverArtLookup(null, 1L, null)
        Assert.assertNull(onlyArtist)

        val artistAndEmptyTitle = coverArtStore.getCoverArtLookup(null, 1L, "")
        Assert.assertNull(artistAndEmptyTitle)

        val onlyTitle = coverArtStore.getCoverArtLookup(null, null, "Cherish")
        Assert.assertNull(onlyTitle)

        val emptyTitle = coverArtStore.getCoverArtLookup(null, null, "")
        Assert.assertNull(emptyTitle)

        val onlyAlbum = coverArtStore.getCoverArtLookup(1L, null, null)
        Assert.assertNull(onlyAlbum)

        verify { coverArtLookupDaoMock wasNot called }
    }

    @Test
    fun getCoverArtLookup_shouldLookupByAlbumAndArtistIfProvidedAndForwardNullableResultFromDao() =
        runBlockingTest {
            coEvery {
                coverArtLookupDaoMock.findByAlbumIdAndArtistId(any(), any())
            } returns null andThen SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL

            val nullResult = coverArtStore.getCoverArtLookup(2L, 3L, null)
            val result = coverArtStore.getCoverArtLookup(2L, 3L, null)

            Assert.assertNull(nullResult)
            Assert.assertEquals(SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL, result)

            coVerify { coverArtLookupDaoMock.findByAlbumIdAndArtistId(any(), any()) }
        }

    @Test
    fun getCoverArtLookup_shouldLookupByArtistAndTrackIfProvidedAndForwardNullableResultFromDao() =
        runBlockingTest {
            coEvery {
                coverArtLookupDaoMock.findByArtistAndTrack(any(), any())
            } returns null andThen SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL

            val nullResult = coverArtStore.getCoverArtLookup(null, 3L, "Cherish")
            val result = coverArtStore.getCoverArtLookup(null, 3L, "Cherish")

            Assert.assertNull(nullResult)
            Assert.assertEquals(SampleCoverArtLookup.COVER_ART_LOOKUP_POJO_FULL, result)

            coVerify { coverArtLookupDaoMock.findByArtistAndTrack(any(), any()) }
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

