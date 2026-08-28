package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry

@Dao
abstract class GenreDao : BaseDao<GenreEntry>() {

    internal fun createGenreEntry(genre: String) = GenreEntry(name = genre)

    @Query("SELECT genre_id FROM genre WHERE name = :genre")
    internal abstract suspend fun findIdByName(genre: String): Long?
}

