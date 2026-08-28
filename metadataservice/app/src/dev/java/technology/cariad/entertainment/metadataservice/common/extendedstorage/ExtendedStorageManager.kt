/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Modifications:
 * - 2026-02-06: Added try-catch for ExtStorageManager to handle system framework conflicts
 * - 2026-02-06: Fallback to internal storage when ESM unavailable
 */
package technology.cariad.entertainment.metadataservice.common.extendedstorage

import android.content.Context
import androidx.preference.PreferenceManager
import com.volkswagenag.storagemanager.ExtStorageManager
import com.volkswagenag.storagemanager.ExtStorageManagerFactory
import technology.cariad.entertainment.metadataservice.stubs.fw.tracing.TraceLevels
import technology.cariad.entertainment.metadataservice.common.log.logger

private const val EXTERNAL_PARTITION = "metadata"
private const val ACCESS_MODE_READ_WRITE = 1
private const val KEY_ESM_ENABLED = "esm_enabled"

class ExtendedStorageManager(private val context: Context) {

    private val log by logger()

    /**
     * Initializes the extended storage partition for the app's data.
     * Upon completion the passed lambda function is invoked with the file system path.
     * If the partition is not available, null is passed to the lambda.
     * @param onInitialized
     */
    fun initialize(onInitialized: (String?) -> Unit) {
        val sharedPreference = PreferenceManager.getDefaultSharedPreferences(context)
        // LOCAL BUILD: ESM disabled by default (no VW StorageManager available)
        val isEsmActive = sharedPreference.getBoolean(KEY_ESM_ENABLED, false)

        if (isEsmActive) {
            try {
                ExtStorageManagerFactory.createExtStorageManager(
                    context
                ) { extStorageManager: ExtStorageManager ->
                    val folderMap = extStorageManager.getUsageFolder(
                        EXTERNAL_PARTITION, ACCESS_MODE_READ_WRITE
                    )
                    folderMap?.get(ACCESS_MODE_READ_WRITE).also { folder ->
                        log.log(
                            if (folder == null) TraceLevels.FATAL else TraceLevels.INFO,
                            "initialize",
                            null
                        ) {
                            "partition=$EXTERNAL_PARTITION access mode=$ACCESS_MODE_READ_WRITE " +
                                    "path=$folder"
                        }
                        if (folder != null) {
                            sharedPreference.edit()?.let {
                                it.putBoolean(KEY_ESM_ENABLED, true)
                                it.commit()
                            }
                        }
                        onInitialized(folder)
                    }
                }
            } catch (e: Throwable) {
                // LOCAL BUILD: System framework conflicts with our stub - fall back to internal storage
                log.warn(this@ExtendedStorageManager::initialize.name) {
                    "ExtStorageManager failed (system framework conflict): ${e.message} - using internal storage"
                }
                onInitialized(context.filesDir.absolutePath)
            }
        } else {
            onInitialized(context.filesDir.absolutePath)
            log.debug(this@ExtendedStorageManager::initialize.name) {
                "ESM is disabled, erased and the internal cache is activated."
            }
        }
    }
}

