package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.AlbumDao
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleAlbum
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AlbumStoreTest {
    private val albumName = SampleAlbum.ALBUM_ENTRY_1.albumName

    private val albumDaoMock: AlbumDao = mockk()

    private val albumStore = AlbumStore(albumDaoMock)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun getAlbumIdByName_shouldForwardNullableResultFromDao() = runBlockingTest {
        coEvery { albumDaoMock.findAlbumIdByName(any()) } returns null andThen 1L

        val firstAlbumId = albumStore.getAlbumIdByName(albumName)
        val secondAlbumId = albumStore.getAlbumIdByName(albumName)

        Assert.assertNull(firstAlbumId)
        Assert.assertEquals(1L, secondAlbumId)
    }

    @Test
    fun selectOrInsertAlbum_shouldForwardResultFromDao() = runBlockingTest {
        coEvery { albumDaoMock.selectOrInsertAlbum(any()) } returns 1L

        val albumId = albumStore.selectOrInsertAlbum(albumName)

        Assert.assertEquals(1L, albumId)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

