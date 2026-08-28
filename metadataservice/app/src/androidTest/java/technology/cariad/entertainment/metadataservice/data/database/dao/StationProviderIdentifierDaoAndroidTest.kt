package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import technology.cariad.entertainment.metadataservice.sampledata.SampleProviderIdentifier
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationProviderIdentifierDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationDao: StationDao
    private lateinit var providerIdentifierDao: ProviderIdentifierDao
    private lateinit var stationProviderIdentifierDao: StationProviderIdentifierDao

    private var stationId = -1L
    private var providerIdentifierId_1 = -1L
    private var providerIdentifierId_2 = -1L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        stationDao = database.getStationDao()
        providerIdentifierDao = database.getProviderIdentifierDao()
        stationProviderIdentifierDao = database.getStationProviderIdentifierDao()

        runBlocking {
            stationId = stationDao.insert(StationEntry())
            providerIdentifierId_1 = providerIdentifierDao.insertProviderIdentifier(SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1)
            providerIdentifierId_2 = providerIdentifierDao.insertProviderIdentifier(SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_2)
            stationProviderIdentifierDao.insert(StationProviderIdentifierEntry(stationId, providerIdentifierId_1))
            stationProviderIdentifierDao.insert(StationProviderIdentifierEntry(stationId, providerIdentifierId_2))
        }
    }

    @Test
    fun deleteByStationId_shouldRemoveStationProviderIdentifierEntries() = runBlocking {
        val deletedRows = stationProviderIdentifierDao.deleteByStationId(stationId)
        Assert.assertEquals(2, deletedRows)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

