package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artist",
    indices = [
        Index(
            name = "index_artist_name",
            value = ["artist_name"],
            unique = true
        )
    ]
)
data class ArtistEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "artist_id")
    val artistId: Long = 0,

    @ColumnInfo(name = "artist_name")
    val artistName: String
)
