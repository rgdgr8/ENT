package technology.cariad.entertainment.metadataservice.contentprovider

import android.net.Uri
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.nio.file.Path
import java.nio.file.Paths

class UriPathMapper(
    private val rootFolder: String,
    private val uriPathFolderMap: Map<String, String>
) {
    private val log by logger()

    private val folderUriPathMapping: Map<String, String> by lazy {
        uriPathFolderMap.entries.associateBy({ it.value }) { it.key }
    }

    /**
     * Converts a [Uri] to the absolute file system path the resource it points to is stored under.
     * @param uri The [Uri] being converted.
     * @return file system [Path] if the [Uri] could be converted successfully, otherwise null
     */
    fun uriToFilePath(uri: Uri): Path? {
        val file = uri.lastPathSegment ?: return null
        val uriPath = uri.path ?: return null

        val pathWithoutFile = uriPath.removeSuffix(file).trim('/')
        val folder = uriPathFolderMap[pathWithoutFile] ?: return null

        return Paths.get(rootFolder, folder, file)
    }

    /**
     * tbd
     */
    fun filePathToUri(authority: String, path: Path): Uri {
        val pathWithoutFileName = path.toString().removeSuffix(path.fileName.toString())
        val subFolder =
            Paths.get(rootFolder).relativize(Paths.get(pathWithoutFileName)).toString()
        val uriPath = folderUriPathMapping[subFolder] ?: return Uri.EMPTY
        val file = path.fileName

        return try {
            Uri.parse("content://$authority/$uriPath/$file")
        } catch (e: NullPointerException) {
            log.error("filePathToUri") { "failed to convert file path to uri. ${e.message}" }
            Uri.EMPTY
        }
    }
}

