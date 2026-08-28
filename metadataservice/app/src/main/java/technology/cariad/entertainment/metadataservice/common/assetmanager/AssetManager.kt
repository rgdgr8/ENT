package technology.cariad.entertainment.metadataservice.common.assetmanager

import android.content.Context
import technology.cariad.entertainment.metadataservice.common.io.calcFileHash
import technology.cariad.entertainment.metadataservice.common.io.safeMkdirs
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.contentToHexString
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Paths
import kotlin.io.path.Path

class AssetManager(private val context: Context) {

    private val log by logger()

    /**
     * Copies the bundled asset denoted by [filename] from [sourceDir] to [destDir].
     * The asset is copied if either the file doesn't exist at [destDir]
     * or if the file at [destDir] differs from the bundled asset file.
     * Any missing folders along [destDir] are created as needed.
     */
    fun installAsset(filename: String, sourceDir: String, destDir: String) {
        try {
            val assetPath = "${Paths.get(sourceDir, filename)}"
            val targetPath = "${Paths.get(destDir, filename)}"
            val targetFile = File(targetPath)

            createDirectory(destDir)

            val assetHash = calculateAssetHash(assetPath)

            if (verifyFile(targetFile, assetHash)) return

            copyAsset(assetPath, targetFile)

            if (!verifyFile(targetFile, assetHash)) {
                throw IOException("checksum mismatch")
            }
        } catch (e: IOException) {
            log.error(this::installAsset.name) {
                "failed to unpack $filename: ${e.message}"
            }

            IRCLogger.appGenericError("${e.message}", "failed to unpack $filename")
        }
    }

    private fun createDirectory(destDir: String) {
        with(File(destDir)) {
            if (!exists()) {
                safeMkdirs(Path(destDir))
            }
        }
    }

    private fun calculateAssetHash(assetPath: String): ByteArray {
        context.assets.open(assetPath).use {
            return calcFileHash(it)
        }
    }

    private fun calculateFileHash(file: File): ByteArray {
        file.inputStream().use {
            return calcFileHash(it)
        }
    }

    private fun verifyFile(targetFile: File, assetHash: ByteArray): Boolean {
        if (!targetFile.exists()) return false

        val targetHash = calculateFileHash(targetFile)

        return assetHash.contentEquals(targetHash).also {
            if (it) {
                log.info(this::installAsset.name) {
                    "target and asset file match: file=${targetFile.name}"
                }
            } else {
                log.warn(this::installAsset.name) {
                    "target and asset file mismatch: file=${targetFile.name} " +
                            "asset hash=${assetHash.contentToHexString()} " +
                            "target hash=${targetHash.contentToHexString()}"
                }
            }
        }
    }

    private fun copyAsset(src: String, dest: File) {
        FileOutputStream(dest, false).use { out ->
            context.assets.open(src).use {
                it.copyTo(out)
            }
            out.flush()
            out.fd.sync()
        }

        log.info(this::copyAsset.name) {
            "copied file=${dest.name}"
        }
    }
}

