package technology.cariad.entertainment.metadataservice.contentprovider

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.ParcelFileDescriptor
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import technology.cariad.entertainment.metadataservice.service.AccessManager
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.Path

class MetadataImageProviderTest {

    private lateinit var provider: MetadataImageProvider

    private lateinit var tempFile: File
    private val mockContext = mockk<Context>(relaxed = true)
    private val mockPackageManager = mockk<PackageManager>(relaxed = true)
    private val mockUri = mockk<Uri>(relaxed = true)
    private val fakeUid = 1234

    companion object {
        private const val URI_AUTHORITY = "technology.cariad.entertainment.metadataservice.MetadataService.ImageProvider"
    }

    @Before
    fun setup() {
        mockkStatic(Binder::class)
        mockkStatic(ParcelFileDescriptor::class)
        mockkObject(MetadataServiceApp)
        mockkObject(AccessManager)

        MetadataServiceApp.appComponent = mockk(relaxed = true)

        every { Binder.getCallingUid() } returns fakeUid
        every {
            ParcelFileDescriptor.open(any(), any(), any(), any())
        } returns mockk()
        every { MetadataServiceApp.appComponent.getContext() } returns mockContext
        every { MetadataServiceApp.appComponent.getFileStorageRoot() } returns "test"
        every { mockContext.packageManager } returns mockPackageManager
        every { mockPackageManager.getPackagesForUid(any()) } returns arrayOf("test.package")

        provider = MetadataImageProvider()

        provider.attachInfo(mockContext, null)

        tempFile = File.createTempFile("test", ".jpg")
        tempFile.writeText("dummy data")
        tempFile.deleteOnExit()
    }

    @After
    fun teardown() {
        unmockkConstructor(UriPathMapper::class)
        unmockkAll()
        tempFile.delete()
    }

    @Test
    fun `onCreate starts HandlerThread`() {
        val result = provider.onCreate()
        assertTrue(result)
    }

    @Test
    fun `openFile MetadataServiceApp not initialized`() {
        every { MetadataServiceApp.isInitialized } returns false

        val result = provider.openFile(mockUri, "r")
        assertNull(result)
    }

    @Test
    fun testMockUriPathMapperInCompanion() {
        val mockUriPathMapper = mockk<UriPathMapper>()
        every { mockUriPathMapper.uriToFilePath(mockUri) } returns Path("/mock/path")

        mockkObject(MetadataImageProvider.Companion)
        every { MetadataImageProvider.Companion.uriPathMapper } returns mockUriPathMapper

        every { MetadataServiceApp.isInitialized } returns true
        every { mockUri.authority } returns URI_AUTHORITY
        every { AccessManager.checkUriPermission(fakeUid, mockUri) } returns true

        provider.onCreate()
        provider.openFile(mockUri, "r")

        verify { mockUriPathMapper.uriToFilePath(mockUri) }
    }

    @Test
    fun testOnCloseListenerWithNull() {
        val mockUriPathMapper = mockk<UriPathMapper>()
        val fakeFile = File.createTempFile("test", ".jpg").apply {
            writeText("mock")
            deleteOnExit()
        }

        every { mockUriPathMapper.uriToFilePath(mockUri) } returns fakeFile.toPath()
        mockkObject(MetadataImageProvider.Companion)
        every { MetadataImageProvider.Companion.uriPathMapper } returns mockUriPathMapper

        every { MetadataServiceApp.isInitialized } returns true
        every { mockUri.authority } returns URI_AUTHORITY
        every { AccessManager.checkUriPermission(fakeUid, mockUri) } returns true

        val listenerSlot = slot<ParcelFileDescriptor.OnCloseListener>()

        val mockPfd = mockk<ParcelFileDescriptor>()
        every {
            ParcelFileDescriptor.open(any(), any(), any(), capture(listenerSlot))
        } returns mockPfd

        provider.onCreate()
        provider.openFile(mockUri, "r")

        listenerSlot.captured.onClose(null)
    }

    @Test
    fun `openFile triggers FileDescriptorDetachedException in onClose`() {
        val mockUriPathMapper = mockk<UriPathMapper>()
        val fakeFile = File.createTempFile("test", ".jpg").apply {
            writeText("mock")
            deleteOnExit()
        }

        every { mockUriPathMapper.uriToFilePath(mockUri) } returns fakeFile.toPath()
        mockkObject(MetadataImageProvider.Companion)
        every { MetadataImageProvider.Companion.uriPathMapper } returns mockUriPathMapper

        every { MetadataServiceApp.isInitialized } returns true
        every { mockUri.authority } returns URI_AUTHORITY
        every { AccessManager.checkUriPermission(fakeUid, mockUri) } returns true

        val listenerSlot = slot<ParcelFileDescriptor.OnCloseListener>()

        val mockPfd = mockk<ParcelFileDescriptor>()
        every {
            ParcelFileDescriptor.open(any(), any(), any(), capture(listenerSlot))
        } returns mockPfd

        provider.onCreate()
        provider.openFile(mockUri, "r")

        val exception = ParcelFileDescriptor.FileDescriptorDetachedException()
        listenerSlot.captured.onClose(exception)

    }

    @Test
    fun `getUriForFile uses real companion object and lazy init`() {
        val fakeUri = mockk<Uri>()

        mockkObject(PathConfigParser)
        every { PathConfigParser.getPathConfig(mockContext) } returns mockk(relaxed = true)

        val path = "/mock/path"

        mockkConstructor(UriPathMapper::class)
        every { anyConstructed<UriPathMapper>().filePathToUri(any(), any()) } returns fakeUri

        val result = MetadataImageProvider.getUriForFile(path)

        assertEquals(fakeUri, result)

        verify { anyConstructed<UriPathMapper>().filePathToUri(URI_AUTHORITY, Paths.get(path)) }
    }

    @Test(expected = SecurityException::class)
    fun `openFile throws if authority invalid`() {
        every { MetadataServiceApp.isInitialized } returns true
        every { mockUri.authority } returns "invalid.authority"
        provider.openFile(mockUri, "r")
    }

    @Test(expected = SecurityException::class)
    fun `openFile throws if no permission`() {
        every { MetadataServiceApp.isInitialized } returns true
        every { mockUri.authority } returns URI_AUTHORITY
        every { AccessManager.checkUriPermission(fakeUid, mockUri) } returns false
        provider.openFile(mockUri, "r")
    }

    @Test(expected = SecurityException::class)
    fun `openFile throws if mode not read`() {
        every { MetadataServiceApp.isInitialized } returns true
        every { mockUri.authority } returns URI_AUTHORITY
        every { AccessManager.checkUriPermission(fakeUid, mockUri) } returns true
        provider.openFile(mockUri, "w")
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `query throws UnsupportedOperationException`() {
        provider.query(mockUri, null, null, null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `insert throws UnsupportedOperationException`() {
        provider.insert(mockUri, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `update throws UnsupportedOperationException`() {
        provider.update(mockUri, null, null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `delete throws UnsupportedOperationException`() {
        provider.delete(mockUri, null, null)
    }

    @Test
    fun `getType returns null`() {
        val result = provider.getType(mockUri)
        assertNull(result)
    }
}

