package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val YESTERDAY = 1_589_013_742L //09.05.2020 - 10:42:22
private const val TODAY = 1_589_100_142L     //10.05.2020 - 10:42:22
private const val TOMORROW = 1_589_186_542L  //11.05.2020 - 10:42:22
private const val DAY_AFTER_TOMORROW = 1_589_272_942L  //12.05.2020 - 10:42:22

private const val RETRY_AFTER_LIMIT = 2 * 24 * 60 * 60L;

class RequestLogicTest {

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun updateRequired_shouldReturnTrueOnExpirationDateMissing() {
        val cached = LifeCycleDataEntity()
        cached.expirationDate = null

        val update = RequestLogic.updateRequired(cached, 0)

        Assert.assertTrue(update)
    }

    @Test
    fun updateRequired_shouldReturnTrueAfterExpirationDateExceeded() {
        val cached = LifeCycleDataEntity()
        cached.expirationDate = YESTERDAY

        val update = RequestLogic.updateRequired(cached, TODAY)

        Assert.assertTrue(update)
    }

    @Test
    fun updateRequired_shouldReturnFalseBeforeExpirationDateExceeded() {
        val cached = LifeCycleDataEntity()
        cached.expirationDate = TOMORROW

        val update = RequestLogic.updateRequired(cached, TODAY)

        Assert.assertFalse(update)
    }


    @Test
    fun updateAllowed_shouldReturnFalseBeforeRetryAfterExceeded() {
        val cached = LifeCycleDataEntity()
        cached.retryAfter = TOMORROW

        val update = RequestLogic.updatedAllowed(cached, TODAY)

        Assert.assertFalse(update)
    }

    @Test
    fun updateAllowed_shouldReturnTrueAfterRetryAfterExceeded() {
        val cached = LifeCycleDataEntity()
        cached.retryAfter = YESTERDAY

        val update = RequestLogic.updatedAllowed(cached, TODAY)

        Assert.assertTrue(update)
    }

    @Test
    fun updateAllowed_shouldReturnTrueOnRetryAfterMissing() {
        val cached = LifeCycleDataEntity()
        cached.retryAfter = null

        val update = RequestLogic.updatedAllowed(cached, TODAY)

        Assert.assertTrue(update)
    }

    @Test
    fun updateAllowed_shouldReturnTrueAfterRetryAfterLimitExceeded() {
        val cached = LifeCycleDataEntity()
        cached.retryAfter = DAY_AFTER_TOMORROW + 1

        val update = RequestLogic.updatedAllowed(cached, TODAY)

        Assert.assertTrue(update)
    }

    @Test
    fun updateAllowed_shouldReturnFalseBeforeRetryAfterLimitExceeded() {
        val cached = LifeCycleDataEntity()
        cached.retryAfter = DAY_AFTER_TOMORROW

        val update = RequestLogic.updatedAllowed(cached, TODAY)

        Assert.assertFalse(update)
    }

    @Test
    fun retryDelay_shouldEraseStatusWhenRetryStrategyIsNoneAndStrategyNotChanged() {
        val cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.NONE)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.NONE)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(0, status.retryCount)
        Assert.assertEquals(null, status.retryAfter)
    }

    @Test
    fun retryDelay_shouldAddDelayWhenFirstRetryAndRetryStrategyIsOnceAndStrategyNotChanged() {
        val cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.ONCE, retryCount = 0)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.ONCE, retryDelay = 5000)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(1, status.retryCount)
        Assert.assertEquals(TODAY + 5000, status.retryAfter)
    }

    @Test
    fun retryDelay_shouldNotRetryAgainWhenRetryStrategyIsOnceAndStrategyNotChanged() {
        val cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.ONCE, retryCount = 1)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.ONCE, retryDelay = 5000)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(1, status.retryCount)
        Assert.assertEquals(null, status.retryAfter)
    }

    @Test
    fun retryDelay_shouldAddDelayWhenRetryStrategyIsDelayedAndStrategyNotChanged() {
        val cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryCount = 0)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryDelay = 5000)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(1, status.retryCount)
        Assert.assertEquals(TODAY + 5000, status.retryAfter)
    }

    @Test
    fun retryDelay_shouldAddDelayAgainWhenRetryStrategyIsDelayedAndStrategyNotChanged() {
        var cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryCount = 0)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryDelay = 5000)

        RequestLogic.retryDelay(cached, status, TODAY)

        cached = status

        RequestLogic.retryDelay(cached, status, TODAY + 5000)

        Assert.assertEquals(2, status.retryCount)
        Assert.assertEquals(TODAY + 5000 + 5000, status.retryAfter)
    }

    @Test
    fun retryDelay_shouldAddExponentialDelayWhenRetryStrategyIsExponentialAndStrategyNotChanged() {
        var cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.EXPONENTIAL, retryCount = 0)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.EXPONENTIAL, retryDelay = 1000)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(1, status.retryCount)
        Assert.assertEquals(TODAY + 1000, status.retryAfter)

        cached = status
        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(2, status.retryCount)
        Assert.assertEquals(TODAY + 2000, status.retryAfter)

        cached = status
        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(3, status.retryCount)
        Assert.assertEquals(TODAY + 4000, status.retryAfter)
    }

    @Test
    fun retryDelay_shouldResetRetryCounterWhenStrategyChanged() {
        val cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.EXPONENTIAL, retryCount = 10)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.ONCE)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(1, status.retryCount)
    }

    @Test
    fun retryDelay_shouldRestrictRetryDelayAboveLimit() {
        var cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.EXPONENTIAL, retryCount = 8)
        var status = LifeCycleDataEntity(retryStrategy = RetryStrategy.EXPONENTIAL, retryDelay = 1000)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(RETRY_AFTER_LIMIT, status.retryAfter!! - TODAY)

        cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryCount = 0)
        status = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryDelay = RETRY_AFTER_LIMIT + 1)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(RETRY_AFTER_LIMIT, status.retryAfter!! - TODAY)
    }

    @Test
    fun retryDelay_shouldNotRestrictRetryDelayBelowLimit() {
        val cached = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryCount = 0)
        val status = LifeCycleDataEntity(retryStrategy = RetryStrategy.DELAYED, retryDelay = RETRY_AFTER_LIMIT - 1)

        RequestLogic.retryDelay(cached, status, TODAY)

        Assert.assertEquals(RETRY_AFTER_LIMIT - 1, status.retryAfter!! - TODAY)
    }
}

