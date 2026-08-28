package technology.cariad.entertainment.metadataservice.common.database.android

import technology.cariad.entertainment.metadataservice.common.io.changeOwner
import technology.cariad.entertainment.metadataservice.common.io.randomHexString
import technology.cariad.entertainment.metadataservice.common.io.safeMkdirs
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

class DatabaseUtils

private val log by logger(DatabaseUtils::class)

/**
 * The extended storage manager is not able to apply proper file permissions to files
 * created by the SQLite database. Hence we have to ensure here all files within the database
 * folder are writable both by the owner as well as the group, so they are accessible from all
 * user profiles.
 */
fun createDatabaseFiles(databaseFile: Path) {
    val databaseShmPath = Paths.get("${databaseFile}-shm")
    val databaseWalPath = Paths.get("${databaseFile}-wal")

    if (!databaseFile.parent.exists()) {
        safeMkdirs(databaseFile.parent)

        log.info(::createDatabaseFiles.name) {
            "created database folder=${databaseFile.parent}"
        }
    }

    val tmpDir = Paths.get("${databaseFile.parent.parent}/db_tmp")
    if (!tmpDir.exists()) {
        safeMkdirs(tmpDir)

        log.info(::createDatabaseFiles.name) {
            "created database tmp folder=${tmpDir}"
        }
    }

    createDatabaseFile(databaseFile, tmpDir)
    createDatabaseFile(databaseShmPath, tmpDir)
    createDatabaseFile(databaseWalPath, tmpDir)

    cleanupTemp(tmpDir)
}

/**
 * Deletes all files related to the SQLite database.
 */
fun deleteDatabaseFiles(databaseFile: Path) {
    val databaseFiles = listOf(
        databaseFile,
        Paths.get("${databaseFile}-shm"),
        Paths.get("${databaseFile}-wal")
    )

    databaseFiles.forEach { file ->
        val present = Files.deleteIfExists(file)

        log.info(::deleteDatabaseFiles.name) {
            "delete database file: file=$file present=$present"
        }
    }
}

private fun createDatabaseFile(file: Path, tmpDir: Path) {
    val permissions = "rw-rw----"

    if (!file.exists()) {
        val suffix = randomHexString(5)
        val tempFile = Paths.get("$tmpDir/${file.fileName}_$suffix")

        tempFile.toFile().createNewFile()
        changeOwner(tempFile, permissions)

        Files.move(tempFile, file, StandardCopyOption.ATOMIC_MOVE)

        log.info(::createDatabaseFile.name) {
            "created missing database file=$file"
        }
    }
}

private fun cleanupTemp(dir: Path) {
    dir.toFile().listFiles()?.onEach {
        try {
            it.delete()
        } catch (e: FileSystemException) {
            log.warn(::cleanupTemp.name) {
                "failed to delete file ${it.name}: message=${e.message}"
            }
        }
    }
}

