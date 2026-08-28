package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "station_etags",
    indices = [
        Index(
            name = "index_station_etags_station_id",
            value = ["station_id"]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = StationEntry::class,
            parentColumns = ["station_id"],
            childColumns = ["station_id"],
            onDelete = CASCADE
        )
    ],
    primaryKeys = ["station_id"]
)
data class ETagsEntry(
    @ColumnInfo(name = "station_id")
    val stationId: Long,

    @ColumnInfo(name = "bearer_list_etag")
    val bearerListETag: String? = null,

    @ColumnInfo(name = "metadata_etag")
    val metadataETag: String? = null,

    @ColumnInfo(name = "streams_etag")
    val streamsETag: String? = null,

    @ColumnInfo(name = "logo_etag")
    val logoETag: String? = null,

    @ColumnInfo(name = "logo_small_etag")
    val logoSmallETag: String? = null
)

