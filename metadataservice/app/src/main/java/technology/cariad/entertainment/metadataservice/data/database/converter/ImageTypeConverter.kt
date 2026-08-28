package technology.cariad.entertainment.metadataservice.data.database.converter

import androidx.room.TypeConverter
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType

/**
 * Type converter to use [ImageType] with Room.
 */
class ImageTypeConverter {

    /**
     * Converts an [ImageType] enum value into an int.
     * @param type
     * @return [Int]
     */
    @TypeConverter
    fun fromLogoType(type: ImageType): Int {
        return type.value
    }

    /**
     * Attempts to convert [value] into an [ImageType] enum value.
     * @param value
     * @return The corresponding enum value or [ImageType.UNKNOWN] if [value] doesn't map to a
     *         valid enum value.
     */
    @TypeConverter
    fun toLogoType(value: Int): ImageType {
        return ImageType.values().find { it.value == value } ?: ImageType.UNKNOWN
    }
}

