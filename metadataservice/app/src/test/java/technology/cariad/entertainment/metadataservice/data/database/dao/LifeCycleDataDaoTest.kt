package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class LifeCycleDataDaoTest {

    private var lifeCycleDataDao: LifeCycleDataDao = mockk(relaxed = true)

    @Test
    fun createLifeCycleDataEntry_shouldConvertEntityToEntry() {

        every { lifeCycleDataDao.createLifeCycleDataEntry(any(),any()) } answers { callOriginal() }

        val lifeCycleDataEntry = lifeCycleDataDao.createLifeCycleDataEntry(
            0,
            LifeCycleDataEntity(
                expirationDate = 900,
                retryStrategy = RetryStrategy.NONE,
                retryDelay = 100,
                retryCount = 2,
                retryAfter = 300,
                lastRequested = 7678254
            )
        )

        val expectedLifeCycleDataEntry =
            LifeCycleDataEntry(
                lastRequested = 7678254,
                validUntil = 900,
                nextRetry = 300,
                lastRetryStatus = RetryStrategy.NONE,
                retryCount = 2,
                retryDelay = 100
            )
        Assert.assertEquals(expectedLifeCycleDataEntry, lifeCycleDataEntry)
    }
}
