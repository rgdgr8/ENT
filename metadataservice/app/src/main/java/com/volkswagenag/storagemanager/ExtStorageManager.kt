package com.volkswagenag.storagemanager

import android.content.Context
import android.util.Log

/**
 * LOCAL BUILD STUB: Replaces com.volkswagenag.storagemanager.ExtStorageManager
 * 
 * In local builds, returns the app's internal files directory instead of external storage.
 */
class ExtStorageManager(private val context: Context) {
    private val tag = "mds/ESMStub"
    
    /**
     * Get usage folder for the specified partition.
     * LOCAL BUILD: Returns internal app storage path.
     * 
     * @param partition The partition name (ignored in local build)
     * @param accessMode The access mode (ignored in local build)
     * @return Map with access mode key and internal files directory path
     */
    fun getUsageFolder(partition: String, accessMode: Int): Map<Int, String>? {
        Log.d(tag, "getUsageFolder(partition=$partition, accessMode=$accessMode) - STUB: Using internal storage")
        return mapOf(accessMode to context.filesDir.absolutePath)
    }
}
