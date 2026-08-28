package technology.cariad.entertainment.metadataservice.data.database

import android.content.Context
import androidx.room.Room
import technology.cariad.entertainment.metadataservice.common.database.android.SQLiteDatabaseFactoryDecorator
import technology.cariad.entertainment.metadataservice.common.database.android.createDatabaseFiles
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.nio.file.Path

class CachingDatabaseFactory private constructor() {

    companion object {

        private val log by logger(CachingDatabaseFactory::class)

        /**
         * Creates a new [CachingDatabase]
         */
        fun create(
            context: Context,
            sqLiteDatabaseFactoryDecorator: SQLiteDatabaseFactoryDecorator,
            file: Path
        ): CachingDatabase {
            return try {
                createDatabase(context, sqLiteDatabaseFactoryDecorator, file).also {
                    it.openHelper.writableDatabase
                }
            } catch (e: IllegalStateException) {

                log.error(this::create.name) {
                    "recreate corrupted database: message=${e.message}"
                }

                IRCLogger.appError(IRCLogger.DB_CORRUPTION_ERROR)

                context.deleteDatabase("$file")
                createDatabase(context, sqLiteDatabaseFactoryDecorator, file)
            }
        }

        private fun createDatabase(
            context: Context,
            sqLiteDatabaseFactoryDecorator: SQLiteDatabaseFactoryDecorator,
            file: Path,
        ): CachingDatabase {

            createDatabaseFiles(file)

            return Room
                .databaseBuilder(context, CachingDatabase::class.java, "$file")
                .fallbackToDestructiveMigration()
                .openHelperFactory(sqLiteDatabaseFactoryDecorator)
                .build()
        }
    }
}

