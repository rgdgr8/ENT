package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy

class RetryBehaviorMapper : Mapper<RetryBehavior, RetryStrategy>() {
    override fun mapValue(value: RetryBehavior): RetryStrategy =
        when (value) {
            RetryBehavior.NONE -> RetryStrategy.NONE
            RetryBehavior.ONCE -> RetryStrategy.ONCE
            RetryBehavior.DELAYED -> RetryStrategy.DELAYED
            RetryBehavior.EXPONENTIAL -> RetryStrategy.EXPONENTIAL
        }
}

