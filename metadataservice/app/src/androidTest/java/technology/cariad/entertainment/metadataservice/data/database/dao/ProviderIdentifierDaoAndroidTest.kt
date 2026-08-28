package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val EXTERNAL_IDENTIFIER = "AABBCCDDEEFF"

@RunWith(AndroidJUnit4::class)
class ProviderIdentifierDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var providerIdentifierDao: ProviderIdentifierDao
    private var expectedProviderIdentifierId = 0L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        providerIdentifierDao = database.getProviderIdentifierDao()

        runBlocking {
            expectedProviderIdentifierId = providerIdentifierDao.insert(
                ProviderIdentifierEntry(
                    origin = ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                    externalId = EXTERNAL_IDENTIFIER
                )
            )
        }
    }

    @Test
    fun findIdByOriginAndExternalId_shouldReturnProviderIdentifierIdForExistingProviderIdentifier() =
        runBlocking {
            val providerIdentifierId = providerIdentifierDao.findIdByOriginAndExternalId(
                ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                EXTERNAL_IDENTIFIER
            )

            Assert.assertEquals(expectedProviderIdentifierId, providerIdentifierId)
        }

    @Test
    fun findIdByOriginAndExternalId_shouldReturnNullForMissingProviderIdentifier() = runBlocking {
        val genreId = providerIdentifierDao.findIdByOriginAndExternalId(
            ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
            "Meow"
        )

        Assert.assertNull(genreId)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

