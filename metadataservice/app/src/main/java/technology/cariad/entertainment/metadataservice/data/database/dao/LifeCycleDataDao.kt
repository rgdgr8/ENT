package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity

@Dao
abstract class LifeCycleDataDao : BaseDao<LifeCycleDataEntry>() {

    internal fun createLifeCycleDataEntry(
        lifeCycleDataId: Long = 0,
        lifeCycleData: LifeCycleDataEntity
    ) =  LifeCycleDataEntry(
            lifeCycleDataId = lifeCycleDataId,
            lastRequested = lifeCycleData.lastRequested,
            validUntil = lifeCycleData.expirationDate,
            nextRetry = lifeCycleData.retryAfter,
            lastRetryStatus = lifeCycleData.retryStrategy,
            retryCount = lifeCycleData.retryCount,
            retryDelay = lifeCycleData.retryDelay
        )

    @Query("SELECT * FROM life_cycle_data WHERE life_cycle_data_id = :lifeCycleDataId")
    internal abstract suspend fun findById(lifeCycleDataId: Long): LifeCycleDataEntry

    @Query("DELETE FROM life_cycle_data WHERE life_cycle_data_id = :lifeCycleDataId")
    internal abstract suspend fun deleteById(lifeCycleDataId: Long)
}
