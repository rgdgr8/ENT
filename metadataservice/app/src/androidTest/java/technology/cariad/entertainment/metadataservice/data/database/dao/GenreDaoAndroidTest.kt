package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val GENRE_ROCK = "Rock"

@RunWith(AndroidJUnit4::class)
class GenreDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var genreDao: GenreDao
    private var expectedGenreId = 0L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        genreDao = database.getGenreDao()

        runBlocking {
            expectedGenreId = genreDao.insert(GenreEntry(0, GENRE_ROCK))
        }
    }

    @Test
    fun findIdByName_shouldReturnGenreIdForExistingGenre() = runBlocking {
        val genreId = genreDao.findIdByName(GENRE_ROCK)

        Assert.assertEquals(expectedGenreId, genreId)
    }

    @Test
    fun findIdByName_shouldReturnNullForMissingGenre() = runBlocking {
        val genreId = genreDao.findIdByName("Meow")

        Assert.assertNull(genreId)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

