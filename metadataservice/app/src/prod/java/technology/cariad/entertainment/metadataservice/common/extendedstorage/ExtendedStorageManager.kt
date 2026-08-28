package technology.cariad.entertainment.metadataservice.common.extendedstorage

import android.content.Context
import com.volkswagenag.storagemanager.ExtStorageManager
import com.volkswagenag.storagemanager.ExtStorageManagerFactory
import technology.cariad.entertainment.metadataservice.stubs.fw.tracing.TraceLevels
import technology.cariad.entertainment.metadataservice.common.log.logger

private const val EXTERNAL_PARTITION = "metadata"
private const val ACCESS_MODE_READ_WRITE = 1

class ExtendedStorageManager(private val context: Context) {

    private val log by logger()

    /**
     * Initializes the extended storage partition for the app's data.
     * Upon completion the passed lambda function is invoked with the file system path.
     * If the partition is not available, null is passed to the lambda.
     * @param onInitialized
     */
    fun initialize(onInitialized: (String?) -> Unit) {
        ExtStorageManagerFactory.createExtStorageManager(
            context
        ) { extStorageManager: ExtStorageManager ->
            val folderMap = extStorageManager.getUsageFolder(
                EXTERNAL_PARTITION, ACCESS_MODE_READ_WRITE
            )
            folderMap?.get(ACCESS_MODE_READ_WRITE).also { folder ->
                log.log(
                    if (folder == null) TraceLevels.FATAL else TraceLevels.INFO, "initialize", null
                ) {
                    "partition=$EXTERNAL_PARTITION access mode=$ACCESS_MODE_READ_WRITE " +
                            "path=$folder"
                }
                onInitialized(folder)
            }
        }
    }
}

