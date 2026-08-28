package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.pojo.CompleteRadioStation

@Dao
abstract class StationDao : BaseDao<StationEntry>() {

    internal fun createStationEntry(
        stationId: Long = 0,
        lifeCycleDataId: Long? = null
    ) =
        StationEntry(
            stationId = stationId,
            lifeCycleDataId = lifeCycleDataId
        )

    @Query("SELECT * FROM station WHERE station_id = :stationId")
    internal abstract suspend fun findByStationId(stationId: Long): StationEntry

    @Query("DELETE FROM station WHERE station_id = :stationId")
    internal abstract suspend fun deleteByStationId(stationId: Long): Int

    @Query("SELECT COUNT(station_id) FROM station")
    internal abstract suspend fun getCount(): Int

    @Transaction
    @Query("SELECT * FROM station WHERE station_id = :stationId")
    internal abstract suspend fun findCompleteById(stationId: Long): CompleteRadioStation?

    @Transaction
    @Query("SELECT station.station_id FROM station INNER JOIN life_cycle_data ON " +
            "station.life_cycle_data_id = life_cycle_data.life_cycle_data_id ORDER BY " +
            "life_cycle_data.last_requested ASC LIMIT 1")
    internal abstract suspend fun findLeastRecentlyUsedId(): Long?
}

