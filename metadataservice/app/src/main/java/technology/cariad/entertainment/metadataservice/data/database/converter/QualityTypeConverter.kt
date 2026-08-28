package technology.cariad.entertainment.metadataservice.data.database.converter

import androidx.room.TypeConverter
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity.Companion.QualityType

/**
 * Type converter to use [QualityType] with Room.
 */
class QualityTypeConverter {

    /**
     * Converts a [QualityType] enum value into an int.
     * @param quality
     * @return [Int]
     */
    @TypeConverter
    fun fromQualityType(quality: QualityType): Int {
        return quality.value
    }

    /**
     * Attempts to convert [value] into a [QualityType] enum value.
     * @param value
     * @return The corresponding enum value or [QualityType.UNKNOWN] if [value] doesn't map to a
     *         valid enum value.
     */
    @TypeConverter
    fun toQualityType(value: Int): QualityType {
        return QualityType.values().find { it.value == value } ?: QualityType.UNKNOWN
    }
}
