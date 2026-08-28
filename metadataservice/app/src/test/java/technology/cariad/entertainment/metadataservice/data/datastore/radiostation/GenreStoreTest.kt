package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.GenreDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationGenreRankDao
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val STATION_ID = 1L
private const val INVALID_DATA_INDEX = -1L

class GenreStoreTest {

    private val genreDaoMock: GenreDao = mockk(relaxed = true)
    private val stationGenreRankDaoMock: StationGenreRankDao = mockk(relaxed = true)

    private val genreStore: GenreStore = GenreStore(genreDaoMock, stationGenreRankDaoMock)
    private val genreStoreSpy = spyk(genreStore)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun updateGenres_shouldDeleteStationGenresAndInsertNewOnesIfGenresNotEmpty() =
        runBlockingTest {

            val genres = listOf("Rock", "Metal")

            coEvery { stationGenreRankDaoMock.deleteByStationId(any()) } returns 1
            coEvery { genreStoreSpy.insertGenres(any(), any()) } returns listOf(1L, 2L)

            genreStoreSpy.updateGenres(STATION_ID, genres)

            coVerifyOrder {
                stationGenreRankDaoMock.deleteByStationId(STATION_ID)
                genreStoreSpy.insertGenres(genres, STATION_ID)
            }
        }

    @Test
    fun updateGenres_shouldDeleteStationGenresAndInsertNothingIfGenresIsEmpty() =
        runBlockingTest {

            val genres = listOf<String>()

            coEvery { stationGenreRankDaoMock.deleteByStationId(any()) } returns 1

            genreStoreSpy.updateGenres(STATION_ID, genres)

            coVerify { stationGenreRankDaoMock.deleteByStationId(STATION_ID) }
            coVerify(inverse = true) { genreStoreSpy.insertGenres(genres, STATION_ID) }
        }

    @Test
    fun deleteByStationId_shouldDeleteStationGenreRanks() =
        runBlockingTest {

            coEvery { stationGenreRankDaoMock.deleteByStationId(any()) } returns 1

            genreStore.deleteByStationId(STATION_ID)

            coVerify { stationGenreRankDaoMock.deleteByStationId(STATION_ID) }
        }

    @Test
    fun insertGenres_shouldCreateStationGenresAndInsertThem() = runBlockingTest {

        val genres = listOf("Rock", "Metal")
        val stationGenreRanks =
            listOf(
                StationGenreRankEntry(STATION_ID, 1L, 0),
                StationGenreRankEntry(STATION_ID, 2L, 1)
            )

        coEvery { genreStoreSpy.getGenreRanks(any(), any()) } returns stationGenreRanks
        coEvery { stationGenreRankDaoMock.insert(any<List<StationGenreRankEntry>>()) } returns
                listOf(
                    1L,
                    2L
                )

        val genreRanks = genreStoreSpy.insertGenres(genres, STATION_ID)

        Assert.assertEquals(listOf(1L, 2L), genreRanks)

        coVerify {
            genreStoreSpy.getGenreRanks(genres, STATION_ID)
            stationGenreRankDaoMock.insert(stationGenreRanks)
        }
    }

    @Test
    fun getGenreRanks_shouldReturnStationGenreRankEntries() = runBlockingTest {

        val genres = listOf("Rock", "Metal")
        val genreEntry1 = GenreEntry(2L, "Rock")
        val genreEntry2 = GenreEntry(3L, "Metal")
        val stationGenreRankEntry1 = stationGenreRankDaoMock.createGenreRankEntry(
            STATION_ID,
            2L,
            0
        )
        val stationGenreRankEntry2 = stationGenreRankDaoMock.createGenreRankEntry(
            STATION_ID,
            3L,
            1
        )
        val stationGenreRanks = listOf(stationGenreRankEntry1, stationGenreRankEntry2)

        every { genreDaoMock.createGenreEntry(any()) } returns genreEntry1 andThen genreEntry2
        coEvery { genreStoreSpy.selectOrInsertGenre(any()) } returns 2L andThen 3L
        every {
            stationGenreRankDaoMock.createGenreRankEntry(any(), any(), any())
        } returns stationGenreRankEntry1 andThen stationGenreRankEntry2

        val genreRanksEntries = genreStoreSpy.getGenreRanks(genres, STATION_ID)

        Assert.assertEquals(stationGenreRanks, genreRanksEntries)

        verify {
            genreDaoMock.createGenreEntry("Rock")
            genreDaoMock.createGenreEntry("Metal")
            stationGenreRankDaoMock.createGenreRankEntry(STATION_ID, 2L, 0)
            stationGenreRankDaoMock.createGenreRankEntry(STATION_ID, 3L, 1)
        }
        coVerify {
            genreStoreSpy.selectOrInsertGenre(genreEntry1)
            genreStoreSpy.selectOrInsertGenre(genreEntry2)
        }
    }

    @Test
    fun selectOrInsertGenre_shouldInsertGenreIfNotFoundAndReturnItsId() = runBlockingTest {

        val genre = GenreEntry(2, "Rock")

        coEvery { genreDaoMock.findIdByName(any()) } returns null
        coEvery { genreDaoMock.insert(any<GenreEntry>()) } returns 2

        val genreId = genreStore.selectOrInsertGenre(genre)

        Assert.assertFalse(genreId == INVALID_DATA_INDEX)

        coVerify {
            genreDaoMock.findIdByName(genre.name)
            genreDaoMock.insert(genre)
        }
    }

    @Test
    fun selectOrInsertGenre_shouldReturnGenreIdIfGenreIsFound() = runBlockingTest {

        val genre = GenreEntry(2, "Rock")

        coEvery { genreDaoMock.findIdByName(any()) } returns 2

        val genreId = genreStore.selectOrInsertGenre(genre)

        Assert.assertEquals(2, genreId)

        coVerify { genreDaoMock.findIdByName(genre.name) }
        coVerify(inverse = true) { genreDaoMock.insert(genre) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

