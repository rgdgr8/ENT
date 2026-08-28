package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry

@Dao
abstract class StationGenreRankDao : BaseDao<StationGenreRankEntry>() {

    internal fun createGenreRankEntry(stationId: Long, genreId: Long, position: Int) =
        StationGenreRankEntry(stationId = stationId, genreId = genreId, position = position)

    @Query("DELETE FROM station_genre_rank WHERE station_id = :stationId")
    internal abstract suspend fun deleteByStationId(stationId: Long): Int
}

