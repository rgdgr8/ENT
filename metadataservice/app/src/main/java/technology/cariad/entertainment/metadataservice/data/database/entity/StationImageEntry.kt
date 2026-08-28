package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(
    tableName = "station_image",
    indices = [
        Index(
            name = "index_station_image_image_id",
            value = ["image_id"]
        ),
        Index(
            name = "index_station_image_station_id",
            value = ["station_id"]
        )
    ],
    foreignKeys = [
        ForeignKey(
            entity = ImageEntry::class,
            parentColumns = ["image_id"],
            childColumns = ["image_id"]
        ),
        ForeignKey(
            entity = StationEntry::class,
            parentColumns = ["station_id"],
            childColumns = ["station_id"],
            onDelete = CASCADE
        )
    ],
    primaryKeys = ["station_id", "image_id"]
)
data class StationImageEntry(

    @ColumnInfo(name = "image_id")
    val imageId: Long,

    @ColumnInfo(name = "station_id")
    val stationId: Long
)

