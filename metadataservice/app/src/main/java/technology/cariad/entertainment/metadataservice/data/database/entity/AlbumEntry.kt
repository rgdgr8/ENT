package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "album",
    indices = [
        Index(
            name = "index_album_name",
            value = ["album_name"],
            unique = true
        )
    ]
)
data class AlbumEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "album_id")
    val albumId: Long = 0,

    @ColumnInfo(name = "album_name")
    val albumName: String
)
