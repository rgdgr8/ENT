package de.esolutions.magicadapter.stapi

import android.content.Context
import android.util.Log
import de.esolutions.coreservices.csm.service.systemservice.DisableReasonsApiCallback

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.magicadapter.stapi.DisableReasonsApiBinding
 */
class DisableReasonsApiBinding(private val context: Context) {
    
    private val tag = "[DisableReasonsApiBinding-STUB]"
    
    fun connect(callback: ServiceConnectionCallback, flags: Int) {
        Log.i(tag, "connect() - STUB: flags=$flags")
        // Simulate successful connection
        android.os.Handler(context.mainLooper).postDelayed({
            callback.serviceConnected()
        }, 100)
    }
    
    fun disconnect() {
        Log.i(tag, "disconnect() - STUB")
    }
    
    fun registerDisableReasonsApiCallback(callback: DisableReasonsApiCallback, clientName: String) {
        Log.i(tag, "registerDisableReasonsApiCallback() - STUB: clientName=$clientName")
        // Simulate empty disable reasons list
        android.os.Handler(context.mainLooper).postDelayed({
            try {
                callback.updateDisableReasons(emptyList())
            } catch (e: Exception) {
                Log.w(tag, "Failed to update disable reasons: ${e.message}")
            }
        }, 100)
    }
    
    fun unregisterDisableReasonsApiCallback(clientName: String) {
        Log.i(tag, "unregisterDisableReasonsApiCallback() - STUB: clientName=$clientName")
    }
    
    fun hasDisableReasons(serviceName: String): Boolean {
        Log.i(tag, "hasDisableReasons() - STUB: serviceName=$serviceName, returning false")
        return false
    }
}
