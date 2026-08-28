package technology.cariad.entertainment.metadataservice.data.filesystem

import technology.cariad.entertainment.metadataservice.common.io.countFilesInDirectory
import technology.cariad.entertainment.metadataservice.common.io.getFilesInDirectory
import technology.cariad.entertainment.metadataservice.common.io.safeDelete
import technology.cariad.entertainment.metadataservice.common.io.safeMkdirs
import technology.cariad.entertainment.metadataservice.common.io.safeWrite
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.contentprovider.MetadataImageProvider
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.pathString

/**
 * Implementation of the [IImageDataStore] interface. All image files are stored under the provided
 * [basePath]. Files names for the images have to be unique.
 * @param basePath The root folder to store images in.
 * @see IImageDataStore
 */
open class ImageDataStore(final override val basePath: String) :
    IImageDataStore {

    private val log by logger()

    init {
        val path = Path(basePath)
        if (!path.exists()) {
            safeMkdirs(path)
        }
    }

    /**
     * @see [IImageDataStore.saveImage]
     */
    override fun saveImage(data: ByteArray, fileName: String): String? {
        val path = buildFilePath(fileName)

        if (path.exists()) {
            log.warn(this::saveImage.name) { "file $fileName already exists" }
            return null
        }

        log.info(this::saveImage.name) {
            "save file path=${path.pathString} size=${path.toFile().length()}B"
        }

        safeWrite(path, data)
        return loadImageUri(fileName)
    }

    /**
     * @see [IImageDataStore.loadImageUri]
     */
    override fun loadImageUri(fileName: String): String? =
        with(buildFilePath(fileName)) {
            var encodedUri = ""

            if (exists()) {
                try {
                    encodedUri = MetadataImageProvider.getUriForFile(this.pathString).toString()

                    log.info(this@ImageDataStore::loadImageUri.name) {
                        "load file path=${this.pathString} " +
                                "uri=encodedUri=$encodedUri " +
                                "size=${toFile().length()}B"
                    }
                } catch (e: IllegalArgumentException) {
                    log.error("loadImageUri") {
                        "failed to retrieve uri for $fileName. ${e.message}"
                    }
                }
            }
            encodedUri
        }

    /**
     * @see [IImageDataStore.deleteImage]
     */
    override fun deleteImage(fileName: String): Boolean {
        val imageFile = buildFilePath(fileName)
        if (!imageFile.exists()) {
            log.info(this::deleteImage.name) { "file does not exist: $fileName" }
            return true
        }

        log.info(this::deleteImage.name) { "delete file: $fileName" }

        return safeDelete(buildFilePath(fileName))
    }

    /**
     * @see [IImageDataStore.getImageFilesCount]
     */
    override fun getImageFilesCount(): Int {
        return countFilesInDirectory(Path(basePath))
    }

    /**
     * @see [IImageDataStore.pruneImageFiles]
     */
    override fun pruneImageFiles(fileNamesInDb: List<String>, fileCountDifference: Int){
        val fileNamesInFolder = getFilesInDirectory(Path(basePath))
        val redundantFiles = fileNamesInFolder.subtract(fileNamesInDb.toSet())

        if (fileCountDifference == redundantFiles.size) {
            redundantFiles.forEach{ fileName->
                log.info(this::pruneImageFiles.name){
                    "File $fileName is not present in DB, deleting..."
                }

                deleteImage(fileName)
            }
        }
        else
        {
            log.info(this::pruneImageFiles.name){
                "Mismatch number of redundant files: given $fileCountDifference, " +
                        "actually counted ${redundantFiles.size}, pruning not performed"
            }
        }
    }

    private fun buildFilePath(fileName: String) = Path("$basePath${File.separator}$fileName")
}

