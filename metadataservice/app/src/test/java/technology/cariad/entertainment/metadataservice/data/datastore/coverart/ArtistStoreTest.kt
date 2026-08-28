package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.ArtistDao
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleArtist
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ArtistStoreTest {
    private val artistName = SampleArtist.ARTIST_ENTRY_1.artistName

    private val artistDaoMock: ArtistDao = mockk()

    private val artistStore = ArtistStore(artistDaoMock)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun getArtistIdByName_shouldForwardNullableResultFromDao() = runBlockingTest {
        coEvery { artistDaoMock.findArtistIdByName(any()) } returns null andThen 1L

        val firstArtistId = artistStore.getArtistIdByName(artistName)
        val secondArtistId = artistStore.getArtistIdByName(artistName)

        Assert.assertNull(firstArtistId)
        Assert.assertEquals(1L, secondArtistId)
    }

    @Test
    fun selectOrInsertArtist_shouldForwardResultFromDao() = runBlockingTest {
        coEvery { artistDaoMock.selectOrInsertArtist(any()) } returns 1L

        val artistId = artistStore.selectOrInsertArtist(artistName)

        Assert.assertEquals(1L, artistId)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

