package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.StationProviderIdentifierEntry

@Dao
abstract class StationProviderIdentifierDao : BaseDao<StationProviderIdentifierEntry>() {

    internal fun createStationProviderIdentifierEntry(stationId: Long, providerIdentifierId: Long) =
        StationProviderIdentifierEntry(stationId = stationId, providerId = providerIdentifierId)

    @Query("DELETE FROM station_provider_identifier WHERE station_id = :stationId")
    internal abstract suspend fun deleteByStationId(stationId: Long): Int
}

