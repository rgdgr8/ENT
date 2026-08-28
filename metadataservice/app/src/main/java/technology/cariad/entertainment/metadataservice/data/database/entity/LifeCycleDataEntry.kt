package technology.cariad.entertainment.metadataservice.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import technology.cariad.entertainment.metadataservice.data.database.converter.RetryStrategyConverter
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy

@TypeConverters(RetryStrategyConverter::class)
@Entity(
    tableName = "life_cycle_data",
    indices = [
        Index(
            name = "index_life_cycle_data_last_requested",
            value = ["last_requested"]
        )
    ]
)
data class LifeCycleDataEntry(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "life_cycle_data_id")
    val lifeCycleDataId: Long = 0,

    @ColumnInfo(name = "last_requested")
    var lastRequested: Long,

    @ColumnInfo(name = "valid_until")
    val validUntil: Long? = null,

    @ColumnInfo(name = "last_retry_status")
    val lastRetryStatus: RetryStrategy? = null,

    @ColumnInfo(name = "next_retry")
    val nextRetry: Long? = null,

    @ColumnInfo(name = "retry_delay")
    val retryDelay: Long? = null,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int? = null
)

