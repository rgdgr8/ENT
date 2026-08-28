package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

private const val GENRE_ROCK = "Rock"

class GenreDaoTest {

    private val genreDao: GenreDao = mockk()

    @Test
    fun createBearerEntries_shouldConvertEntitiesToEntries() {

        every { genreDao.createGenreEntry(any()) } answers { callOriginal() }

        val genreEntry = genreDao.createGenreEntry(GENRE_ROCK)

        Assert.assertEquals(GenreEntry(0, GENRE_ROCK), genreEntry)
    }
}

