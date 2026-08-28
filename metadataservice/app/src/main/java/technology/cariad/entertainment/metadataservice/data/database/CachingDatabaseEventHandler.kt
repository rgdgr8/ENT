package technology.cariad.entertainment.metadataservice.data.database

import androidx.sqlite.db.SupportSQLiteDatabase
import technology.cariad.entertainment.metadataservice.common.database.android.IDatabaseEventListener
import technology.cariad.entertainment.metadataservice.common.database.android.createDatabaseFiles
import technology.cariad.entertainment.metadataservice.common.database.android.deleteDatabaseFiles
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

class CachingDatabaseEventHandler(private val databaseFile: Path) : IDatabaseEventListener {

    private val log by logger()

    override fun afterCreate(db: SupportSQLiteDatabase) {
        log.info(this::afterCreate.name) { "create database triggers" }

        createTriggers(db)
    }

    override fun afterCorruption(db: SupportSQLiteDatabase) {
        log.info(this::afterCorruption.name) { "recreate database files" }

        deleteDatabaseFiles(databaseFile)
        createDatabaseFiles(databaseFile)

        printDbFiles(this::afterCorruption.name)
    }

    private fun printDbFiles(funcName: String) {
        val databaseFiles = databaseFile.parent.listDirectoryEntries().joinToString()
        log.info(funcName) { "files in database directory: $databaseFiles" }
    }
}

