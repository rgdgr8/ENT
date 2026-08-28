package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity

@Dao
abstract class StreamDao : BaseDao<StreamEntry>() {

    internal fun createStreamEntry(
        stream: StationStreamEntity,
        stationId: Long,
        position: Int
    ) = StreamEntry(
        stationId = stationId,
        position = position,
        url = stream.url,
        offsetMs = stream.offsetMillis,
        quality = stream.quality
    )

    @Query("DELETE FROM stream WHERE station_id = :stationId")
    internal abstract suspend fun deleteByStationId(stationId: Long): Int
}

