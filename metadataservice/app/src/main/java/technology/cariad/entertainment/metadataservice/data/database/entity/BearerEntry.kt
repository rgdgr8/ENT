package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bearer",
    indices = [
        Index(
            name = "index_bearer_station_id",
            value = ["station_id"]
        ),
        Index(
            name = "index_bearer_uri",
            value = ["uri"],
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
data class BearerEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "bearer_id")
    val bearerId: Long = 0,

    @ColumnInfo(name = "station_id")
    val stationId: Long,

    @ColumnInfo(name = "uri")
    val uri: String
)

