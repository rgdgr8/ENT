package technology.cariad.entertainment.metadataservice.data.database.converter

import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy
import org.junit.Assert
import org.junit.Test

class RetryStrategyConverterTest {

    private val retryStrategyConverter = RetryStrategyConverter()

    @Test
    fun should_convertRetryStrategyToInt() {
        val none = retryStrategyConverter.fromRetryStrategy(RetryStrategy.NONE)
        val once = retryStrategyConverter.fromRetryStrategy(RetryStrategy.ONCE)
        val delayed = retryStrategyConverter.fromRetryStrategy(RetryStrategy.DELAYED)
        val exponential = retryStrategyConverter.fromRetryStrategy(RetryStrategy.EXPONENTIAL)

        Assert.assertEquals(0, none)
        Assert.assertEquals(1, once)
        Assert.assertEquals(2, delayed)
        Assert.assertEquals(3, exponential)
    }

    @Test
    fun should_convertIntInRangeToRetryStrategy() {
        val none = retryStrategyConverter.toRetryStrategy(0)
        val once = retryStrategyConverter.toRetryStrategy(1)
        val delayed = retryStrategyConverter.toRetryStrategy(2)
        val exponential = retryStrategyConverter.toRetryStrategy(3)

        Assert.assertEquals(RetryStrategy.NONE, none)
        Assert.assertEquals(RetryStrategy.ONCE, once)
        Assert.assertEquals(RetryStrategy.DELAYED, delayed)
        Assert.assertEquals(RetryStrategy.EXPONENTIAL, exponential)
    }

    @Test
    fun should_convertIntOutOfRangeToUnknownNull() {
        val unknownResult_5 = retryStrategyConverter.toRetryStrategy(-5)
        val unknownResult_1 = retryStrategyConverter.toRetryStrategy(-1)
        val unknownResult4 = retryStrategyConverter.toRetryStrategy(4)
        val unknownResult17 = retryStrategyConverter.toRetryStrategy(17)

        Assert.assertEquals(null, unknownResult_5)
        Assert.assertEquals(null, unknownResult_1)
        Assert.assertEquals(null, unknownResult4)
        Assert.assertEquals(null, unknownResult17)
    }
}

