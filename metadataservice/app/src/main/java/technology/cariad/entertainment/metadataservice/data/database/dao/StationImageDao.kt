package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import technology.cariad.entertainment.metadataservice.data.database.converter.ImageTypeConverter
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType

@Dao
abstract class StationImageDao : BaseDao<StationImageEntry>() {

    internal fun createStationImageEntry(
        imageId: Long,
        stationId: Long
    ) = StationImageEntry(
        imageId = imageId,
        stationId = stationId
    )

    @TypeConverters(ImageTypeConverter::class)
    @Query("SELECT image_id FROM image WHERE image_id in " +
            "(SELECT image_id FROM station_image WHERE station_id = :stationId) AND " +
            "image_type = :imageType")
    internal abstract suspend fun findLogoByStationIdAndType(
        stationId: Long,
        imageType: ImageType
    ): Long?

    @Query("SELECT image_id FROM station_image WHERE station_id = :stationId")
    internal abstract suspend fun findImageIdsByStationId(stationId: Long): List<Long>

    @Query("DELETE FROM station_image WHERE station_id = :stationId")
    internal abstract suspend fun deleteImageByStationId(stationId: Long)
}

