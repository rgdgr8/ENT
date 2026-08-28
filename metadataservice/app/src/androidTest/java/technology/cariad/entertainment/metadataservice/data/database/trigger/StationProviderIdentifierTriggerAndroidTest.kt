package technology.cariad.entertainment.metadataservice.data.database.trigger

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.dao.ProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.testhelper.InMemoryCachingDatabaseFactory
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationProviderIdentifierTriggerAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationProviderIdentifierDao: StationProviderIdentifierDao
    private lateinit var providerIdentifierDao: ProviderIdentifierDao
    private lateinit var stationDao: StationDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = InMemoryCachingDatabaseFactory.create(context)

        stationProviderIdentifierDao = database.getStationProviderIdentifierDao()
        providerIdentifierDao = database.getProviderIdentifierDao()
        stationDao = database.getStationDao()
    }

    @Test
    fun deleteStationProviderIdentifier_shouldDeleteProviderIdentifierIfLastReferenced() =
        runBlocking {
            val providerIdentifierEntry = ProviderIdentifierEntry(
                0,
                ProviderIdentifierEntity.Companion.OriginType.RADIOPLAYER,
                "external_id"
            )
            val providerIdentifierId = providerIdentifierDao.insert(providerIdentifierEntry)

            val stationEntry = StationEntry()
            val stationId = stationDao.insert(stationEntry)

            val stationProviderIdentifierEntry =
                StationProviderIdentifierEntry(stationId, providerIdentifierId)
            stationProviderIdentifierDao.insert(stationProviderIdentifierEntry)

            stationProviderIdentifierDao.deleteByStationId(stationId)

            val providerIdentifierAfterDelete = providerIdentifierDao.findById(providerIdentifierId)

            Assert.assertNull(providerIdentifierAfterDelete)
        }

    @Test
    fun deleteStationGenre_shouldNotDeleteProviderIdentifierIfReferencedByOtherEntries() =
        runBlocking {
            val providerIdentifierEntry = ProviderIdentifierEntry(
                0,
                ProviderIdentifierEntity.Companion.OriginType.RADIOPLAYER,
                "external_id"
            )
            val providerIdentifierId = providerIdentifierDao.insert(providerIdentifierEntry)

            val stationEntry = StationEntry()
            val stationId = stationDao.insert(stationEntry)

            val stationProviderIdentifierEntry =
                StationProviderIdentifierEntry(stationId, providerIdentifierId)
            stationProviderIdentifierDao.insert(stationProviderIdentifierEntry)

            val otherStationEntry = StationEntry()
            val otherStationId = stationDao.insert(otherStationEntry)

            val otherStationProviderIdentifierEntry =
                StationProviderIdentifierEntry(otherStationId, providerIdentifierId)
            stationProviderIdentifierDao.insert(otherStationProviderIdentifierEntry)

            stationProviderIdentifierDao.deleteByStationId(stationId)

            val providerIdentifierAfterDelete = providerIdentifierDao.findById(providerIdentifierId)

            Assert.assertNotNull(providerIdentifierAfterDelete)
        }

    @Test
    fun deleteStationGenre_shouldNotDeleteOtherProviderIdentifiers() = runBlocking {
        val providerIdentifierEntry = ProviderIdentifierEntry(
            0,
            ProviderIdentifierEntity.Companion.OriginType.RADIOPLAYER,
            "external_id"
        )
        val providerIdentifierId = providerIdentifierDao.insert(providerIdentifierEntry)

        val stationEntry = StationEntry()
        val stationId = stationDao.insert(stationEntry)

        val stationProviderIdentifierEntry =
            StationProviderIdentifierEntry(stationId, providerIdentifierId)
        stationProviderIdentifierDao.insert(stationProviderIdentifierEntry)

        val otherProviderIdentifierEntry = ProviderIdentifierEntry(
            0,
            ProviderIdentifierEntity.Companion.OriginType.RADIODNS,
            "other_external_id"
        )
        val otherProviderIdentifierId = providerIdentifierDao.insert(otherProviderIdentifierEntry)

        stationProviderIdentifierDao.deleteByStationId(stationId)

        val otherProviderIdentifierIdAfterDelete =
            providerIdentifierDao.findById(otherProviderIdentifierId)

        Assert.assertNotNull(otherProviderIdentifierIdAfterDelete)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

