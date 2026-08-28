package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "genre",
    indices = [
        Index(
            name = "index_genre_name",
            value = ["name"],
            unique = true
        )
    ]
)
data class GenreEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "genre_id")
    val genreId: Long = 0,

    @ColumnInfo(name = "name")
    val name: String
)

