package technology.cariad.entertainment.metadataservice.data.database.testhelper

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import technology.cariad.entertainment.metadataservice.common.database.android.SQLiteDatabaseFactoryDecorator
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabaseEventHandler
import kotlin.io.path.Path

class InMemoryCachingDatabaseFactory private constructor() {
    companion object {
        fun create(context: Context): CachingDatabase {
            return Room.inMemoryDatabaseBuilder(context, CachingDatabase::class.java)
                .openHelperFactory(
                    SQLiteDatabaseFactoryDecorator(
                        FrameworkSQLiteOpenHelperFactory(),
                        CachingDatabaseEventHandler(
                            Path("${context.cacheDir.path}/metadata_cache.db")
                        )
                    )
                )
                .build()
        }
    }
}

