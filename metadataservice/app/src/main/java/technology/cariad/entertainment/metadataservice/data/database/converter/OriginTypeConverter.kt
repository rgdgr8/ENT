package technology.cariad.entertainment.metadataservice.data.database.converter

import androidx.room.TypeConverter
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity.Companion.OriginType

/**
 * Type converter to use [OriginType] with Room.
 */
class OriginTypeConverter {

    /**
     * Converts an [OriginType] enum value into an int.
     * @param origin
     * @return [Int]
     */
    @TypeConverter
    fun fromOriginType(origin: OriginType): Int {
        return origin.value
    }

    /**
     * Attempts to convert [value] into an [OriginType] enum value.
     * @param value
     * @return The corresponding enum value or [OriginType.UNKNOWN] if [value] doesn't map to a
     *         valid enum value.
     */
    @TypeConverter
    fun toOriginType(value: Int): OriginType {
        return OriginType.values().find { it.value == value } ?: OriginType.UNKNOWN
    }
}
