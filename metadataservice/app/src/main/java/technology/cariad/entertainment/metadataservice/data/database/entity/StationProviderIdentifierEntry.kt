package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

/**
 * Junction table to represent the dependency between a station and its provider identifiers.
 */
@Entity(
    tableName = "station_provider_identifier",
    indices = [
        Index(
            name = "index_station_provider_identifier_station_id",
            value = ["station_id"]
        ),
        Index(
            name = "index_station_provider_identifier_provider_id",
            value = ["provider_id"]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = StationEntry::class,
            parentColumns = ["station_id"],
            childColumns = ["station_id"],
            onDelete = CASCADE
        ),
        ForeignKey(
            entity = ProviderIdentifierEntry::class,
            parentColumns = ["provider_id"],
            childColumns = ["provider_id"]
        )
    ],
    primaryKeys = ["station_id", "provider_id"]
)
data class StationProviderIdentifierEntry(

    @ColumnInfo(name = "station_id")
    val stationId: Long,

    @ColumnInfo(name = "provider_id")
    val providerId: Long
)

