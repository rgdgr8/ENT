package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoverArtLookupDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var coverArtLookupDao: CoverArtLookupDao
    private lateinit var lifeCycleDataDao: LifeCycleDataDao

    private var coverArtLookupId: Long? = null

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        coverArtLookupDao = database.getCoverArtLookupDao()
        lifeCycleDataDao = database.getLifeCycleDataDao()

        runBlocking {
            val lifeCycleId = lifeCycleDataDao.insert(LifeCycleDataEntry(0, 1000))
            coverArtLookupId = coverArtLookupDao.insert(
                CoverArtLookupEntry(
                    0,
                    null,
                    null,
                    null,
                    null,
                    lifeCycleId
                )
            )

            lifeCycleDataDao.insert(LifeCycleDataEntry(0, 500))
        }
    }

    @Test
    fun findLeastRecentlyUsedId_shouldFindOldestCoverArtLookup() {
        var id: Long? = null

        runBlocking {
            id = coverArtLookupDao.findLeastRecentlyUsedId()
        }

        Assert.assertEquals(coverArtLookupId, id)
    }

    @After
    fun tearDown() {
        database.close()
    }
}

