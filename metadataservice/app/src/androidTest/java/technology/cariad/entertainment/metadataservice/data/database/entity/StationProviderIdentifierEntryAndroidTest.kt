package technology.cariad.entertainment.metadataservice.data.database.entity

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.ProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationProviderIdentifierEntryAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var providerIdentifierDao: ProviderIdentifierDao
    private lateinit var stationProviderIdentifierDao: StationProviderIdentifierDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationDao = database.getStationDao()
        providerIdentifierDao = database.getProviderIdentifierDao()
        stationProviderIdentifierDao = database.getStationProviderIdentifierDao()
    }

    @Test
    fun deleteStation_shouldCascadeDeleteStationProviderIdentifier() = runBlocking {
        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val providerIdentifierEntry = ProviderIdentifierEntry(
            origin = ProviderIdentifierEntity.Companion.OriginType.RADIODNS,
            externalId = "external_id"
        )
        val providerIdentifierId = providerIdentifierDao.insert(providerIdentifierEntry)

        val stationProviderIdentifierEntry =
            StationProviderIdentifierEntry(stationId, providerIdentifierId)
        stationProviderIdentifierDao.insert(stationProviderIdentifierEntry)

        stationDao.deleteByStationId(stationId)

        val deleteCount = stationProviderIdentifierDao.deleteByStationId(stationId)

        Assert.assertEquals(0, deleteCount)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

