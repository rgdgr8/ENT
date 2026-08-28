package technology.cariad.entertainment.metadataservice.common.io

import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.contentToHexString
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.util.UUID
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString
import kotlin.random.Random

class FileUtils

val log by logger(FileUtils::class)

private const val BUFFER_SIZE = 8192

/**
 * Creates the directory denoted by [path] including any non-existing parent directories.
 * @param path directory path
 */
fun safeMkdirs(path: Path) {
    if (path.exists()) {
        throw IllegalArgumentException("path must not point to an existing file/folder")
    }

    try {
        val file = path.toFile()
        file.mkdirs()
        fsync(path)
        fsync(path.parent)
    } catch (e: IOException) {
        log.error(::safeWrite.name) { "failed to write file=$path (reason=${e.message})" }
        IRCLogger.appGenericError("failed to write file=$path", "${e.message}")
    }
}

/**
 * Creates and writes [data] to the file denoted by [path] and calls fsync on both the file and its
 * parent directory.
 *
 * @param path file path
 * @param data file content
 * @return true if the operation was successful, false otherwise
 */
fun safeWrite(path: Path, data: ByteArray): Boolean {
    if (path.exists()) {
        throw IllegalArgumentException("path must not point to an existing file/folder")
    }

    val file = path.toFile()

    return try {
        FileOutputStream(file).use {
            it.write(data)
            it.flush()
            it.fd.sync()
            fsync(path.parent)
            true
        }
    } catch (e: IOException) {
        log.error(::safeWrite.name) { "failed to write file=$path (reason=${e.message})" }
        IRCLogger.appGenericError("failed to write file=$path", "${e.message}")
        false
    }
}

/**
 * Deletes the file denoted by the given [path] and fsyncs its parent directory.
 * Must only be called for regular files.
 * @param path file path
 * @return true if the operation was successful, false otherwise
 */
fun safeDelete(path: Path): Boolean {
    if (!path.isRegularFile()) {
        throw IllegalArgumentException("path must denote a regular file")
    }

    return try {
        path.toFile().delete()
        fsync(path.parent)
        true
    } catch (e: IOException) {
        log.error(::safeWrite.name) { "failed to delete file=$path (reason=${e.message})" }
        IRCLogger.appGenericError("failed to delete file=$path", "${e.message}")
        false
    }
}

/**
 * Calls fsync for the given [path].
 * Must only be called with paths denoting directories or regular files.
 *
 * @param path path to fsync
 * @throws IOException if the underlying fsync failed
 * @see FileChannel.force
 */
fun fsync(path: Path) {
    if (!(path.isDirectory() || path.isRegularFile())) {
        throw (IllegalArgumentException("path must denote a directory or a normal file"))
    }

    FileChannel.open(path, StandardOpenOption.READ).use {
        it.force(true)
    }
}

/**
 * Returns a new unique file name based on a [UUID].
 * @param extension file extension
 */
fun generateFilename(extension: String = ""): String = "${UUID.randomUUID()}.$extension"

/**
 * Returns number of files in the specified directory.
 * @param Path object of required directory
 * @return Number of files in the directory on success, 0 otherwise
 */
fun countFilesInDirectory(path: Path): Int {
    return try {
        path.listDirectoryEntries().size
    } catch (e: Exception) {
        log.error(::countFilesInDirectory.name) {
            "failed to count files in dir=$path (reason=${e.message})"
        }
        0
    }
}

/**
 * Returns list of files in the specified directory.
 * @param Path object of required directory
 * @return List<String> of file names in the directory on success, otherwise empty list
 */
fun getFilesInDirectory(path: Path): List<String> {
    return try {
        path.listDirectoryEntries().map { it.fileName.pathString }
    } catch (e: Exception) {
        log.error(::getFilesInDirectory.name) {
            "failed to get files in dir=$path (reason=${e.message})"
        }
        listOf<String>()
    }
}

/**
 * Calculates a SHA-1 hash over the entire data available from the given [InputStream].
 * @param inputStream
 * @return ByteArray containing the hash.
 */
fun calcFileHash(inputStream: InputStream): ByteArray {
    val digest = MessageDigest.getInstance("SHA-1")

    val buffer = ByteArray(BUFFER_SIZE)
    var bytesRead = 0

    while (bytesRead != -1) {
        bytesRead = inputStream.read(buffer)
        if (bytesRead > 0) {
            digest.update(buffer, 0, bytesRead)
        }
    }

    return digest.digest().also {
        log.debug(::calcFileHash.name) {
            it.contentToHexString()
        }
    }
}

/**
 * Changes the permissions of the given file to the given permissions.
 * It must be ensured that the app has the permission to perform this operation.
 */
fun changeOwner(file: Path, permissions: String) {
    log.info(::changeOwner.name) { "set file attributes for: $file to $permissions" }

    val posixPermissions = PosixFilePermissions.fromString(permissions)

    try {
        Files.setPosixFilePermissions(file, posixPermissions)
    } catch (e: FileSystemException) {
        log.error(::changeOwner.name) {
            "failed to set file permissions: message=${e.message}"
        }
    }
}


/**
 * Creates a random hex string of the given length.
 */
fun randomHexString(length: Int): String? {
    val random = Random.Default
    val buffer = StringBuffer()
    while (buffer.length < length) {
        buffer.append(Integer.toHexString(random.nextInt()))
    }
    return buffer.toString().substring(0, length)
}

