/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace e.solutions framework dependencies.
 */
package technology.cariad.entertainment.metadataservice.stubs.fw.tracing

import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.fw.util.commons.tracing.stapi.TraceClient
 * 
 * Static trace client for framework logging.
 * In local builds, all operations delegate to Android Log.
 */
object TraceClient {
    private const val TAG = "mds/TraceClient"
    private val callbackIdCounter = AtomicInteger(0)

    /**
     * Register a trace callback.
     * LOCAL BUILD: No-op, callbacks are not used with Android Log.
     */
    fun registerCallback(callback: ITraceCallback) {
        Log.d(TAG, "registerCallback() - STUB: No-op")
    }

    /**
     * Register a trace callback with a name.
     * LOCAL BUILD: Returns a dummy callback ID.
     * @param name Name for the callback registration
     * @param callback The callback to register
     * @return A dummy callback ID
     */
    fun registerTraceCallback(name: String, callback: ITraceCallback): Int {
        val id = callbackIdCounter.incrementAndGet()
        Log.d(TAG, "registerTraceCallback(name=$name) - STUB: Returning id=$id")
        return id
    }

    /**
     * Unregister a trace callback.
     * LOCAL BUILD: No-op.
     */
    fun unregisterCallback(callback: ITraceCallback) {
        Log.d(TAG, "unregisterCallback() - STUB: No-op")
    }

    /**
     * Unregister a trace callback by ID.
     * LOCAL BUILD: No-op.
     */
    fun unregisterTraceCallback(id: Int) {
        Log.d(TAG, "unregisterTraceCallback(id=$id) - STUB: No-op")
    }
}

