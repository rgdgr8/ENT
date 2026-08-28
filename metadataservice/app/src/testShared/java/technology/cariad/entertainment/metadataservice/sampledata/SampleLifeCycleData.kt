package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy

class SampleLifeCycleData {
    companion object {
        val LIFE_CYCLE_DATA_ENTRY_1 = LifeCycleDataEntry(
            lifeCycleDataId = 10,
            lastRequested = 10,
            validUntil = 10,
            lastRetryStatus = null,
            nextRetry = 10,
            retryCount = 1,
            retryDelay = 20
        )

        val LIFE_CYCLE_DATA_ENTITY_1
            get() = LifeCycleDataEntity(
                0,
                0,
                RetryStrategy.NONE,
                0,
                0,
                0
            )
    }
}
