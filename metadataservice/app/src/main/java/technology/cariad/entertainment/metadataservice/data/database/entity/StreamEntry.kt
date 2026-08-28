package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import technology.cariad.entertainment.metadataservice.data.database.converter.QualityTypeConverter
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity.Companion.QualityType

@TypeConverters(QualityTypeConverter::class)
@Entity(
    tableName = "stream",
    indices = [
        Index(
            name = "index_stream_station_id",
            value = ["station_id"]
        ),
        Index(
            name = "index_stream_station_id_position",
            value = ["station_id", "position"],
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

data class StreamEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "stream_id")
    val streamId: Long = 0,

    @ColumnInfo(name = "station_id")
    val stationId: Long,

    @ColumnInfo(name = "position")
    val position: Int,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "offset_ms")
    val offsetMs: Int? = null,

    @ColumnInfo(name = "quality")
    val quality: QualityType
)
