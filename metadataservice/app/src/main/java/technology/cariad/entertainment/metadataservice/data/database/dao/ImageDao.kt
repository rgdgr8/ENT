package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity

@Dao
abstract class ImageDao : BaseDao<ImageEntry>() {

    internal fun createImageEntry(
        imageId: Long = 0,
        image: ImageEntity
    ) = ImageEntry(
        imageId = imageId,
        //TODO: converter conflicts with @Embedded usage in ImageWithProviderAndMimeType
        imageType = image.imageType!!.value,
        fileName = image.fileName
    )

    @Query("SELECT * FROM image WHERE image_id = :imageId")
    internal abstract suspend fun findImageById(imageId: Long): ImageEntry?

    @Query("DELETE FROM image WHERE image_id = :imageId")
    internal abstract suspend fun deleteById(imageId: Long)

    @Query("SELECT image_id FROM image WHERE file_name = :fileName")
    internal abstract suspend fun findImageIdsByFileName(fileName: String): List<Int>

    @Query("SELECT DISTINCT file_name FROM image WHERE image_type=:imageType ORDER BY file_name")
    internal abstract suspend fun getUniqueFileNamesOfType(imageType: Int): List<String>
}

