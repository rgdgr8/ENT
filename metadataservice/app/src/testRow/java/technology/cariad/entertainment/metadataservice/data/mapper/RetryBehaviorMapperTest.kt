package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy
import org.junit.Assert
import org.junit.Test

class RetryBehaviorMapperTest {
    private val retryBehaviorMapper = RetryBehaviorMapper()

    @Test
    fun mapValue_shouldConvertRetryBehaviorToRetryStrategy() {
        val retryStrategyNone = retryBehaviorMapper.mapValue(RetryBehavior.NONE)
        Assert.assertEquals(RetryStrategy.NONE, retryStrategyNone)

        val retryStrategyOnce = retryBehaviorMapper.mapValue(RetryBehavior.ONCE)
        Assert.assertEquals(RetryStrategy.ONCE, retryStrategyOnce)

        val retryStrategyDelayed = retryBehaviorMapper.mapValue(RetryBehavior.DELAYED)
        Assert.assertEquals(RetryStrategy.DELAYED, retryStrategyDelayed)

        val retryStrategyExponential = retryBehaviorMapper.mapValue(RetryBehavior.EXPONENTIAL)
        Assert.assertEquals(RetryStrategy.EXPONENTIAL, retryStrategyExponential)
    }

    @Test
    fun mapList_shouldConvertRetryBehaviorsToRetryStrategies() {
        val retryStrategies = retryBehaviorMapper.mapList(
            listOf(
                RetryBehavior.NONE,
                RetryBehavior.ONCE,
                RetryBehavior.DELAYED,
                RetryBehavior.EXPONENTIAL
            )
        )

        val expectedRetryStrategies = listOf(
            RetryStrategy.NONE,
            RetryStrategy.ONCE,
            RetryStrategy.DELAYED,
            RetryStrategy.EXPONENTIAL
        )

        Assert.assertEquals(expectedRetryStrategies, retryStrategies)
    }

    @Test
    fun mapList_shouldConvertEmpyListToEmptyList() {
        val emptyList = retryBehaviorMapper.mapList(listOf())

        Assert.assertEquals(listOf<RetryStrategy>(), emptyList)
    }
}

