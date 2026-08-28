package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.StationMetadataEntry
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity

@Dao
abstract class StationMetadataDao : BaseDao<StationMetadataEntry>() {

    internal fun createStationMetadataEntry(
        metadataId: Long = 0,
        metadata: StationMetadataEntity?,
        stationId: Long
    ) =
        StationMetadataEntry(
            metadataId = metadataId,
            shortName = metadata?.shortName,
            mediumName = metadata?.mediumName,
            longName = metadata?.longName,
            description = metadata?.description,
            stationId = stationId
        )

    @Query("SELECT * FROM station_metadata WHERE station_id = :stationId")
    internal abstract suspend fun findByStationId(stationId: Long): StationMetadataEntry?

    @Query("SELECT metadata_id FROM station_metadata WHERE station_id = :stationId")
    internal abstract suspend fun findIdByStationId(stationId: Long): Long?

    @Query("DELETE FROM station_metadata WHERE station_id = :stationId")
    internal abstract suspend fun deleteByStationId(stationId: Long): Int
}
