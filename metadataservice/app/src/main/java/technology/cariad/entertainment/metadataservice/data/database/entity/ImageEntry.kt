package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image")
data class ImageEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "image_id")
    val imageId: Long = 0,

    @ColumnInfo(name = "image_type")
    val imageType: Int,

    @ColumnInfo(name = "file_name")
    val fileName: String
)

