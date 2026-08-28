/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace e.solutions framework dependencies.
 */
package de.esolutions.android.framework.clients

import android.content.Context
import android.util.Log

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.android.framework.clients.AsiAdmin
 * 
 * ASI Admin client for managing ASI service connections.
 * In local builds, this is a no-op stub.
 */
object AsiAdmin {
    private const val TAG = "mds/AsiAdminStub"
    
    private var isStarted = false
    
    /**
     * Start the ASI Admin client.
     * LOCAL BUILD: No-op, always returns true.
     */
    fun start(context: Context): Boolean {
        Log.i(TAG, "start() - STUB: Simulating ASI Admin start")
        isStarted = true
        return true
    }
    
    /**
     * Stop/shutdown the ASI Admin client.
     * LOCAL BUILD: No-op.
     */
    fun stop() {
        Log.i(TAG, "stop() - STUB: Simulating ASI Admin stop")
        isStarted = false
    }
    
    /**
     * Shutdown the ASI Admin client.
     * LOCAL BUILD: No-op.
     */
    fun shutdown() {
        Log.i(TAG, "shutdown() - STUB: Simulating ASI Admin shutdown")
        isStarted = false
    }
    
    /**
     * Check if ASI Admin is started.
     */
    fun isStarted(): Boolean = isStarted
}
