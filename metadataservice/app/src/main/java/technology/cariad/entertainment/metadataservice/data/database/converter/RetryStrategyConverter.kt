package technology.cariad.entertainment.metadataservice.data.database.converter

import androidx.room.TypeConverter
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy

/**
 * Type converter to use [RetryStrategy] with Room.
 */
class RetryStrategyConverter {

    /**
     * Converts an [RetryStrategy] enum value into an int.
     * @param strategy
     * @return [Int]
     */
    @TypeConverter
    fun fromRetryStrategy(strategy: RetryStrategy?): Int {
        return strategy?.value ?: RetryStrategy.NONE.value
    }

    /**
     * Attempts to convert [value] into an [RetryStrategy] enum value.
     * @param value
     * @return The corresponding enum value or null if [value] doesn't map to a valid enum value.
     */
    @TypeConverter
    fun toRetryStrategy(value: Int): RetryStrategy? {
        return RetryStrategy.values().find { it.value == value }
    }
}

