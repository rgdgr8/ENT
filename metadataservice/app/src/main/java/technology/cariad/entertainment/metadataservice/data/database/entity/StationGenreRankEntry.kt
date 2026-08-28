package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index

@Entity(tableName = "station_genre_rank",
    indices = [
        Index(value = ["genre_id"]),
        Index(value = ["station_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = GenreEntry::class,
            parentColumns = ["genre_id"],
            childColumns = ["genre_id"]
        ),
        ForeignKey(
            entity = StationEntry::class,
            parentColumns = ["station_id"],
            childColumns = ["station_id"],
            onDelete = CASCADE
        )
    ],
    primaryKeys = ["station_id", "genre_id"]
)
data class StationGenreRankEntry(

    @ColumnInfo(name = "station_id")
    val stationId: Long,

    @ColumnInfo(name = "genre_id")
    val genreId: Long,

    @ColumnInfo(name = "position")
    val position: Int
)

