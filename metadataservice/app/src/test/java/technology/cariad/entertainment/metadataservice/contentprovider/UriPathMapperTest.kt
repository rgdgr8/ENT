package technology.cariad.entertainment.metadataservice.contentprovider

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Paths

class UriPathMapperTest {

    private val rootFolder = "/root"
    private val uriPathFolderMap = mapOf(
        "images" to "img",
        "documents" to "docs"
    )
    private val uriPathMapper = UriPathMapper(rootFolder, uriPathFolderMap)

    @Test
    fun `uriToFilePath returns correct path`() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "file.jpg"
        every { uri.path } returns "/images/file.jpg"

        val path = uriPathMapper.uriToFilePath(uri)

        val expectedPath = Paths.get(rootFolder, "img", "file.jpg")
        assertEquals(expectedPath, path)
    }

    @Test
    fun `uriToFilePath returns null when lastPathSegment is null`() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns null

        val path = uriPathMapper.uriToFilePath(uri)

        assertNull(path)
    }

    @Test
    fun `uriToFilePath returns null when path is null`() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "file.jpg"
        every { uri.path } returns null

        val path = uriPathMapper.uriToFilePath(uri)

        assertNull(path)
    }

    @Test
    fun `uriToFilePath returns null when folder not in map`() {
        val uri = mockk<Uri>()
        every { uri.lastPathSegment } returns "file.jpg"
        every { uri.path } returns "/unknown/file.jpg"

        val path = uriPathMapper.uriToFilePath(uri)

        assertNull(path)
    }

    @Test(expected = NullPointerException::class)
    fun `filePathToUri returns correct Uri`() {
        val rootFolder = "/root"
        val uriPathFolderMap = mapOf("documents" to "docs")
        val uriPathMapper = UriPathMapper(rootFolder, uriPathFolderMap)

        val path = Paths.get("/root/docs/file.txt")
        val authority = "com.example.app"

        val uri = uriPathMapper.filePathToUri(authority, path)

        assertEquals("content://com.example.app/documents/file.txt", uri.toString())
    }


    @Test(expected = NullPointerException::class)
    fun `filePathToUri returns empty Uri if subfolder not in mapping`() {
        val rootFolder = "/root"
        val uriPathFolderMap = mapOf("documents" to "docs")
        val uriPathMapper = UriPathMapper(rootFolder, uriPathFolderMap)

        val path = Paths.get("/root/unknown/file.txt")
        val authority = "com.example.app"

        val uri = uriPathMapper.filePathToUri(authority, path)

        assertEquals(Uri.parse(""), uri)
    }


}

