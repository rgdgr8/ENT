package com.volkswagenag.storagemanager

import android.content.Context
import android.util.Log

/**
 * LOCAL BUILD STUB: Replaces com.volkswagenag.storagemanager.ExtStorageManagerFactory
 * 
 * Factory for creating ExtStorageManager instances.
 * In local builds, immediately invokes the callback with a stub manager.
 */
object ExtStorageManagerFactory {
    private const val TAG = "mds/ESMFactoryStub"

    /**
     * Create an ExtStorageManager and invoke the callback.
     * LOCAL BUILD: Immediately creates a stub manager and invokes callback.
     * 
     * @param context The Android context
     * @param callback Callback to receive the ExtStorageManager instance
     */
    fun createExtStorageManager(
        context: Context,
        callback: (ExtStorageManager) -> Unit
    ) {
        Log.i(TAG, "createExtStorageManager() - STUB: Creating stub manager")
        val manager = ExtStorageManager(context)
        callback(manager)
    }
}
