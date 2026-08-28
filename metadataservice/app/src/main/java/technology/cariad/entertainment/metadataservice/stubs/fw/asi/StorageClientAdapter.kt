/*
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Author: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Stub implementation to replace e.solutions framework dependencies.
 */
package technology.cariad.entertainment.metadataservice.stubs.fw.asi

import android.os.Handler
import android.os.Looper
import android.util.Log
import de.esolutions.android.framework.clients.IServiceCallback
import de.esolutions.android.framework.clients.types.OneDimShortArrayWrapper

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.fw.android.comm.asi.diag.pers.impl.StorageClientAdapter
 * 
 * This is a stub implementation that simulates ASI Storage connection.
 * In local builds, no actual ASI communication occurs - all operations are no-ops.
 */
class StorageClientAdapter(
    private val instanceId: Int,
    private val listener: IStorageServiceListener
) {
    private val tag = "mds/StorageStub"
    
    private val stubApi = object : StorageApi {
        override fun subscribe(blobId: Int) {
            Log.d(tag, "subscribe(blobId=$blobId) - STUB: No-op")
        }
        
        override fun unsubscribe(blobId: Int) {
            Log.d(tag, "unsubscribe(blobId=$blobId) - STUB: No-op")
        }
        
        override fun get0(blobId: Int) {
            Log.d(tag, "get0(blobId=$blobId) - STUB: Returning default country code DE")
            // FUNCTIONCONFIGURATION_HMI_BLOB_ID = 0x8802
            // Return a blob with country code "DE" (68='D', 69='E') at bytes 0 and 1
            val defaultValue = shortArrayOf(68, 69) // "DE" - Germany
            listener.get0Reply(OptionalValue(blobId, OpResult.op_result_ok, defaultValue))
        }
        
        override fun get1(blobId: Int) {
            Log.d(tag, "get1(blobId=$blobId) - STUB: No-op")
            listener.get1Reply(OptionalValueOneDimArrayObjectWrapper(emptyArray()))
        }
        
        override fun set(blobId: Int, value: OneDimShortArrayWrapper) {
            Log.d(tag, "set(blobId=$blobId) - STUB: No-op")
        }
    }

    fun api(): StorageApi = stubApi

    fun connectService(callback: IServiceCallback) {
        Log.i(tag, "connectService() - STUB: Simulating connection established")
        // Trigger connectionEstablished callback asynchronously to simulate real behavior
        Handler(Looper.getMainLooper()).post {
            callback.connectionEstablished("ASI_STORAGE_STUB", instanceId)
        }
    }

    fun disconnectService() {
        Log.i(tag, "disconnectService() - STUB: Simulating disconnection")
    }

    interface StorageApi {
        fun subscribe(blobId: Int)
        fun unsubscribe(blobId: Int)
        fun get0(blobId: Int)
        fun get1(blobId: Int)
        fun set(blobId: Int, value: OneDimShortArrayWrapper)
    }
}

