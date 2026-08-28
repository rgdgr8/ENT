package technology.cariad.entertainment.metadataservice.common.assetmanager

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import technology.cariad.entertainment.metadataservice.common.io.calcFileHash
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths

private const val ASSET = "testasset.bin"
private const val EMPTY_ASSET = "emptytestasset.bin"
private const val CORRUPTED_ASSET = "corruptedtestasset.bin"

private const val SRC_DIR = "assetmanager"
private const val DEST_DIR = "assetdir"

@RunWith(AndroidJUnit4::class)
class AssetManagerAndroidTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private val assetManager = AssetManager(context)

    private val assetPath = "${Paths.get(SRC_DIR, ASSET)}"
    private val emptyAssetPath = "${Paths.get(SRC_DIR, EMPTY_ASSET)}"
    private val corruptedAssetPath = "${Paths.get(SRC_DIR, CORRUPTED_ASSET)}"
    private val destPath = "${Paths.get(targetContext.dataDir.path, DEST_DIR)}"

    @Test
    fun installAsset_shouldInstallMissingFile() {
        val destFile = "${Paths.get(destPath, ASSET)}"

        assetManager.installAsset(ASSET, SRC_DIR, destPath)

        val assetHash = context.assets.open(assetPath).use { calcFileHash(it) }
        val fileHash = File(destFile).inputStream().use { calcFileHash(it) }

        Assert.assertArrayEquals(assetHash, fileHash)
    }

    @Test
    fun installAsset_shouldReplaceEmptyFile() {
        val destFile = "${Paths.get(destPath, ASSET)}"

        File(destPath).mkdir()
        FileOutputStream(destFile, false).use { out ->
            context.assets.open(emptyAssetPath).use {
                it.copyTo(out)
            }
        }

        val assetHashBeforeInstall = context.assets.open(assetPath).use { calcFileHash(it) }
        val fileHashBeforeInstall = File(destFile).inputStream().use { calcFileHash(it) }

        Assert.assertFalse(assetHashBeforeInstall.contentEquals(fileHashBeforeInstall))

        assetManager.installAsset(ASSET, SRC_DIR, destPath)

        val assetHash = context.assets.open(assetPath).use { calcFileHash(it) }
        val fileHash = File(destFile).inputStream().use { calcFileHash(it) }

        Assert.assertArrayEquals(assetHash, fileHash)
    }

    @Test
    fun installAsset_shouldReplaceCorruptedFile() {
        val destFile = "${Paths.get(destPath, ASSET)}"

        File(destPath).mkdir()
        FileOutputStream(destFile, false).use { out ->
            context.assets.open(corruptedAssetPath).use {
                it.copyTo(out)
            }
        }

        val assetHashBeforeInstall = context.assets.open(assetPath).use { calcFileHash(it) }
        val fileHashBeforeInstall = File(destFile).inputStream().use { calcFileHash(it) }

        Assert.assertFalse(assetHashBeforeInstall.contentEquals(fileHashBeforeInstall))

        assetManager.installAsset(ASSET, SRC_DIR, destPath)

        val assetHash = context.assets.open(assetPath).use { calcFileHash(it) }
        val fileHash = File(destFile).inputStream().use { calcFileHash(it) }

        Assert.assertArrayEquals(assetHash, fileHash)
    }

    @After
    fun tearDown() {
        File(destPath).deleteRecursively()
    }
}

