package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.ETagsEntry

@Dao
abstract class ETagsDao : BaseDao<ETagsEntry>() {

    @Query("UPDATE station_etags SET bearer_list_etag=:etag WHERE station_id = :stationId")
    internal abstract suspend fun updateBearerListETag(stationId: Long, etag: String)

    @Query("UPDATE station_etags SET metadata_etag=:etag WHERE station_id = :stationId")
    internal abstract suspend fun updateMetadataETag(stationId: Long, etag: String)

    @Query("UPDATE station_etags SET streams_etag=:etag WHERE station_id = :stationId")
    internal abstract suspend fun updateStreamsETag(stationId: Long, etag: String)

    @Query("UPDATE station_etags SET logo_etag=:etag WHERE station_id = :stationId")
    internal abstract suspend fun updateLogoETag(stationId: Long, etag: String)

    @Query("UPDATE station_etags SET logo_small_etag=:etag WHERE station_id = :stationId")
    internal abstract suspend fun updateLogoSmallETag(stationId: Long, etag: String)
}
