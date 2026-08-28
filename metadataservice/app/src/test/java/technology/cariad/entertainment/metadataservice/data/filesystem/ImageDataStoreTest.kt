package technology.cariad.entertainment.metadataservice.data.filesystem

import android.content.Context
import android.content.pm.ProviderInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ContentProviderController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import java.io.FileInputStream

// TODO: Maybe all tests requiring a FileProvider should be done on a real Android OS. i.e. as instrumented test on the emulator or real hardware.

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ImageDataStoreTest {

    companion object {
        private const val ROOT_FOLDER = "image_cache/connected_radio/"
        private const val LOGO_NAME = "logo_eagle_80s.png"
        private const val UNKNOWN_LOGO_NAME = "unknown_logo.png"

        private const val AUTHORITY =
            "technology.cariad.entertainment.metadataservice.MetadataService.ImageProvider"
        private const val LOGO_URI =
            "content://technology.cariad.entertainment.metadataservice.MetadataService.ImageProvider/conrad/logo_eagle_80s.png"
    }

    private lateinit var dataStore: ImageDataStore
    private lateinit var context: Context
    private lateinit var sampleLogoData: ByteArray

    private lateinit var contentProviderController: ContentProviderController<FileProvider>
    private lateinit var shadowContentResolver: ShadowContentResolver

    private lateinit var fullLogoDirectory: String
    private lateinit var fullLogoPath: String
    private lateinit var unknownLogoPath: String

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        dataStore = ImageDataStore(ROOT_FOLDER)

        javaClass.classLoader?.getResourceAsStream(LOGO_NAME).use {
            sampleLogoData = it?.readBytes() ?: byteArrayOf()
        }

        val info = ProviderInfo()
        info.authority = AUTHORITY
        info.grantUriPermissions = true
        info.exported = false
        contentProviderController =
            Robolectric.buildContentProvider(FileProvider::class.java).create(info)
        shadowContentResolver = shadowOf(context.contentResolver)

        fullLogoDirectory = "${context.filesDir}${File.separator}$ROOT_FOLDER${File.separator}"
        fullLogoPath = "$fullLogoDirectory$LOGO_NAME"
        unknownLogoPath = "$fullLogoDirectory$UNKNOWN_LOGO_NAME"
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun imageDataStore_shouldCreateFolderStructure() {
        val folder = File(fullLogoDirectory)
        Assert.assertTrue(folder.exists())
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun saveImage_shouldCreateFileWithContent() {
        dataStore.saveImage(sampleLogoData, LOGO_NAME)

        val file = File(fullLogoPath)

        var fileContent: ByteArray
        FileInputStream(file).use {
            fileContent = it.readBytes()
        }

        Assert.assertTrue(file.exists())
        Assert.assertArrayEquals(sampleLogoData, fileContent)
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun saveImage_shouldReturnResolvableFileProviderUri() {
        val uri = dataStore.saveImage(sampleLogoData, LOGO_NAME)

        shadowContentResolver.registerInputStream(
            Uri.parse(LOGO_URI),
            FileInputStream(File(fullLogoPath))
        )

        var fileContent: ByteArray

        context.contentResolver.openInputStream(Uri.parse(uri)).use {
            fileContent = it?.readBytes() ?: byteArrayOf()
        }

        Assert.assertArrayEquals(sampleLogoData, fileContent)
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun saveImage_shouldNotOverwriteFiles() {
        var uri = dataStore.saveImage(sampleLogoData, LOGO_NAME)

        Assert.assertNotEquals(uri, null)

        uri = dataStore.saveImage(byteArrayOf(13, 37, 42), LOGO_NAME)

        Assert.assertEquals(uri, null)

        val file = File(fullLogoPath)

        var fileContent: ByteArray
        FileInputStream(file).use {
            fileContent = it.readBytes()
        }

        Assert.assertTrue(file.exists())
        Assert.assertArrayEquals(sampleLogoData, fileContent)
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun loadImage_shouldReturnResolvableUri() {
        dataStore.saveImage(sampleLogoData, LOGO_NAME)

        val uri = dataStore.loadImageUri(LOGO_NAME)

        shadowContentResolver.registerInputStream(
            Uri.parse(LOGO_URI),
            FileInputStream(File(fullLogoPath))
        )

        var fileContent: ByteArray

        context.contentResolver.openInputStream(Uri.parse(uri)).use {
            fileContent = it?.readBytes() ?: byteArrayOf()
        }

        Assert.assertArrayEquals(sampleLogoData, fileContent)
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun deleteImage_shouldRemoveFile() {
        dataStore.saveImage(sampleLogoData, LOGO_NAME)

        dataStore.deleteImage(LOGO_NAME)

        val file = File(fullLogoPath)

        Assert.assertFalse(file.exists())
    }

    @Test
    @Ignore("Robolectric causes issues on CI server")
    fun deleteImage_shouldDoNothingWhenFileNameNotValid() {
        val file = File(unknownLogoPath)

        Assert.assertFalse(file.exists())

        dataStore.deleteImage(UNKNOWN_LOGO_NAME)

        Assert.assertFalse(file.exists())
    }

    @After
    fun tearDown() {

        // Big oof:
        // The FileProvider has a static cache mapping authorities to file system paths.
        // This is to ensure Uris stay valid even across application restarts. See comments on [FileProvider.PathStrategy].
        // While the authority is statically configured in the manifest, the file system paths change
        // between different test cases executed with the robolectric runner.
        //
        // When executing multiple test runs accessing the FileProvider API, later runs will fail as
        // no longer valid file system paths from the first test case are still cached.
        // When executing each test case individually, everything appears to work just fine.
        //
        // This is a rather hacky way, to clear the static cache between test cases.
        //
        // Note: Don't even think about ever doing something similar in production code!
        val field = FileProvider::class.java.getDeclaredField("sCache")
        field.isAccessible = true
        val obj = field.get(null) as HashMap<*, *>
        obj.clear()
    }
}

