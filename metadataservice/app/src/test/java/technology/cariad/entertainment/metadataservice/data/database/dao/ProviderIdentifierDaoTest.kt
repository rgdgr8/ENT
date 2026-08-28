package technology.cariad.entertainment.metadataservice.data.database.dao

import com.google.common.base.CharMatcher.any
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleProviderIdentifier
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

private const val EXTERNAL_IDENTIFIER = "AABBCCDDEEFF"
private const val FAKE_PROVIDER_ID = 4L

class ProviderIdentifierDaoTest {

    private var providerIdentifierDao: ProviderIdentifierDao = mockk()

    @Test
    fun createProviderIdentifierEntry_shouldConvertEntitiesToEntries() {

        every { providerIdentifierDao.createProviderIdentifierEntry(any()) } answers { callOriginal() }

        val providerIdentifierEntity = providerIdentifierDao.createProviderIdentifierEntry(
            ProviderIdentifierEntity(
                ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                EXTERNAL_IDENTIFIER
            )
        )

        Assert.assertEquals(
            ProviderIdentifierEntry(
                0,
                ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                EXTERNAL_IDENTIFIER
            ), providerIdentifierEntity
        )
    }

    @Test
    fun selectOrInsertProvider() = runBlockingTest {

        coEvery { providerIdentifierDao.findIdByOriginAndExternalId(any(), any()) } returns null
        coEvery { providerIdentifierDao.selectOrInsertProvider(any()) } answers { callOriginal() }
        coEvery { providerIdentifierDao.insert(any<ProviderIdentifierEntry>()) } returns FAKE_PROVIDER_ID

        providerIdentifierDao.selectOrInsertProvider(
            ProviderIdentifierEntry(
                providerId = 1,
                origin = ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                externalId = EXTERNAL_IDENTIFIER
            )
        )

        coVerify {
            providerIdentifierDao.insert(any<ProviderIdentifierEntry>())
        }
    }
}

