package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.GenreDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationGenreRankDao
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry

private const val INVALID_DATA_INDEX = -1L

class GenreStore(
    private val genreDao: GenreDao,
    private val stationGenreRankDao: StationGenreRankDao
) {

    internal suspend fun updateGenres(stationId: Long, genres: List<String>) {
        stationGenreRankDao.deleteByStationId(stationId)
        if (genres.isNotEmpty()) {
            insertGenres(genres, stationId)
        }
    }

    internal suspend fun deleteByStationId(stationId: Long) {
        stationGenreRankDao.deleteByStationId(stationId)
    }

    //TODO: reorder arguments
    internal suspend fun insertGenres(genres: List<String>, stationId: Long): List<Long> {
        val genreRanks = getGenreRanks(genres, stationId)
        return stationGenreRankDao.insert(genreRanks)
    }

    internal suspend fun getGenreRanks(
        genres: List<String>,
        stationId: Long
    ): List<StationGenreRankEntry> = genres.mapIndexed { index, it ->

        val genreEntry = genreDao.createGenreEntry(it)
        val genreId = selectOrInsertGenre(genreEntry)

        stationGenreRankDao.createGenreRankEntry(stationId, genreId, index)
    }

    internal suspend fun selectOrInsertGenre(genre: GenreEntry): Long {
        var id = genreDao.findIdByName(genre.name)
        if (id == null) {
            id = genreDao.insert(genre)
        }
        return id
    }
}

