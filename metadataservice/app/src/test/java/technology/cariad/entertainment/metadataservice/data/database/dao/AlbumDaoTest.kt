package technology.cariad.entertainment.metadataservice.data.database.dao

import com.google.common.base.CharMatcher.any
import technology.cariad.entertainment.metadataservice.data.database.entity.AlbumEntry
import technology.cariad.entertainment.metadataservice.mock.android.LogMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleAlbum.Companion.ALBUM_ENTRY_1
import technology.cariad.entertainment.metadataservice.sampledata.SampleAlbum.Companion.ALBUM_ID_1
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AlbumDaoTest {

    private val albumDao: AlbumDao = mockk(relaxed = true)

    @Before
    fun setup() {

        LogMock()
    }

    @Test
    fun createAlbumEntry_shouldCreateEntryByAlbumName() {

        every { albumDao.createAlbumEntry(any()) } answers { callOriginal() }

        val albumEntry = albumDao.createAlbumEntry("Like a Prayer")
        Assert.assertEquals(ALBUM_ENTRY_1, albumEntry)
    }

    @Test
    fun selectOrInsertAlbum_shouldInsertAlbumAndReturnId() = runBlockingTest {

        coEvery { albumDao.findAlbumIdByName(any()) } returns null
        coEvery { albumDao.insert(any<AlbumEntry>()) } returns ALBUM_ID_1
        every { albumDao.createAlbumEntry("Like a Prayer") } answers { callOriginal()}
        coEvery { albumDao.selectOrInsertAlbum("Like a Prayer") } answers { callOriginal() }

        albumDao.selectOrInsertAlbum("Like a Prayer")

        coVerify {
            albumDao.createAlbumEntry(any())
            albumDao.insert(any<AlbumEntry>())
        }
    }
}

