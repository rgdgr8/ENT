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

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.fw.util.commons.tracing.stapi.ITraceCallback
 * 
 * Interface for trace callbacks - used to receive trace level change notifications
 * and custom callbacks from native code via TraceClient.
 */
interface ITraceCallback {
    /**
     * Called when the trace level changes.
     * LOCAL BUILD: This is a no-op stub.
     */
    fun onTraceLevelChanged(level: Int) {
        Log.d("mds/TraceCallback", "onTraceLevelChanged($level) - STUB")
    }

    /**
     * Execute a trace callback triggered from native code.
     * Used for TLS testing and other cross-layer callbacks.
     * @param cbid Callback identifier
     * @param payload Optional payload data
     */
    fun executeTraceCallback(cbid: Int, payload: ByteArray?) {
        Log.d("mds/TraceCallback", "executeTraceCallback(cbid=$cbid) - STUB")
    }
}

