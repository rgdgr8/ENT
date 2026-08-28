package de.esolutions.magicadapter.stapi

import android.content.Context
import android.util.Log
import de.esolutions.magicadapter.generic.TrustStoreCallback
import java.security.KeyStore
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.magicadapter.stapi.TrustStoreApiBinding
 * Returns the system default TrustManager to allow HTTPS connections.
 */
class TrustStoreApiBinding(private val context: Context) {
    
    private val tag = "[TrustStoreApiBinding-STUB]"
    
    private val defaultTrustManager: X509TrustManager? by lazy {
        try {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )
            trustManagerFactory.init(null as KeyStore?)
            trustManagerFactory.trustManagers
                .filterIsInstance<X509TrustManager>()
                .firstOrNull()
        } catch (e: Exception) {
            Log.e(tag, "Failed to create default TrustManager: ${e.message}")
            null
        }
    }
    
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
    
    fun fetchTrustStore(trustStoreId: String, callback: TrustStoreCallback) {
        Log.i(tag, "fetchTrustStore() - STUB: trustStoreId=$trustStoreId")
        // Return null trust store - we use system defaults via TrustManager
        android.os.Handler(context.mainLooper).postDelayed({
            callback.trustStoreByNameResponse(trustStoreId, null)
        }, 100)
    }
    
    fun fetchTrustManager(operationId: String, callback: TrustStoreCallback) {
        Log.i(tag, "fetchTrustManager() - STUB: operationId=$operationId, returning system default TrustManager")
        // Return the system default TrustManager
        android.os.Handler(context.mainLooper).postDelayed({
            callback.trustStoreManagerResponse(operationId, defaultTrustManager)
        }, 100)
    }
}
