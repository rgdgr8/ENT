package technology.cariad.entertainment.metadataservice.data.database.dao

import android.content.Context
import androidx.room.Room.inMemoryDatabaseBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StationGenreRankDaoAndroidTest {

    private lateinit var database: CachingDatabase
    private lateinit var stationGenreRankDao: StationGenreRankDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = inMemoryDatabaseBuilder(context, CachingDatabase::class.java).build()
        stationGenreRankDao = database.getStationGenreRankDao()
    }

    @Test
    fun test() {}

    @After
    fun tearDown() {
        database.close()
    }
}

