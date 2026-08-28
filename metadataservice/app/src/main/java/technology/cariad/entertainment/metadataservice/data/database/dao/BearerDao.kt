package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity

@Dao
abstract class BearerDao : BaseDao<BearerEntry>() {

    internal fun createBearerEntries(
        bearerList: BearerListEntity, stationId: Long
    ): List<BearerEntry> {
        return bearerList.bearers
            .mapNotNull { it.toBearerString() }
            .map { BearerEntry(uri = it, stationId = stationId) }
    }

    @Query("SELECT station_id FROM bearer WHERE uri = :bearer")
    internal abstract suspend fun findStationIdByBearer(bearer: String): Long?

    @Query("DELETE FROM bearer WHERE station_id = :stationId AND uri NOT IN (:bearers)")
    internal abstract suspend fun deleteMissingBearersForStationId(
        stationId: Long,
        bearers: List<String>
    )

    @Query("DELETE FROM bearer WHERE station_id = :stationId")
    internal abstract suspend fun deleteByStationId(stationId: Long): Int
}

