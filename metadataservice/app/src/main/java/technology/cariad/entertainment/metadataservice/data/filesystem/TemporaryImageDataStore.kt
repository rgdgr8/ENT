package technology.cariad.entertainment.metadataservice.data.filesystem

import technology.cariad.entertainment.metadataservice.common.io.fsync
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.io.File
import kotlin.io.path.Path

/**
 * A specialized image data store, that deletes all content in [basePath] before it can be used.
 */
class TemporaryImageDataStore(basePath: String) :
    ImageDataStore(basePath) {

    val log by logger()

    init {
        val folder = File(basePath)
        folder.listFiles()?.forEach {
            it.deleteRecursively()
        }
        fsync(Path(basePath))

        log.info("init") {
            "cleanup of temporary image folder done: path=$basePath"
        }
    }
}

