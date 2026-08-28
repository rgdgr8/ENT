package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "station_metadata",
    indices = [
        Index(
            name = "index_station_metadata_station_id",
            value = ["station_id"],
            unique = true
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = StationEntry::class,
            parentColumns = ["station_id"],
            childColumns = ["station_id"],
            onDelete = CASCADE
        )
    ]
)
data class StationMetadataEntry (

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "metadata_id")
    val metadataId: Long,

    @ColumnInfo(name = "station_id")
    val stationId: Long,

    @ColumnInfo(name = "short_name")
    val shortName: String? = null,

    @ColumnInfo(name = "medium_name")
    val mediumName: String? = null,

    @ColumnInfo(name = "long_name")
    val longName: String? = null,

    @ColumnInfo(name = "description")
    val description: String? = null
)
