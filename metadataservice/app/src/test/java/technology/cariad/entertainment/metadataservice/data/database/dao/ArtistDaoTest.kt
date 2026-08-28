package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.ArtistEntry
import technology.cariad.entertainment.metadataservice.sampledata.SampleArtist.Companion.ARTIST_ENTRY_1
import technology.cariad.entertainment.metadataservice.sampledata.SampleArtist.Companion.ARTIST_ID_1
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

class ArtistDaoTest {

    private val artistDao: ArtistDao = mockk(relaxed = true)

    @Test
    fun createArtistEntry_shouldCreateEntryByArtistName() {

        every { artistDao.createArtistEntry(any()) } answers { callOriginal() }

        val artistEntry = artistDao.createArtistEntry("Madonna")
        Assert.assertEquals(ARTIST_ENTRY_1, artistEntry)
    }

    @Test
    fun selectOrInsertArtist_shouldInsertArtistAndReturnId() = runBlockingTest {

        coEvery { artistDao.findArtistIdByName(any()) } returns null
        coEvery { artistDao.insert(any<ArtistEntry>()) } returns ARTIST_ID_1
        every { artistDao.createArtistEntry("Madonna") } answers { callOriginal()}
        coEvery { artistDao.selectOrInsertArtist("Madonna") } answers { callOriginal() }

        artistDao.selectOrInsertArtist("Madonna")

        coVerify {
            artistDao.createArtistEntry(any())
            artistDao.insert(any<ArtistEntry>())
        }
    }
}

