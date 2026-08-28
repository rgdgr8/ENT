package technology.cariad.entertainment.metadataservice.common.codingprovider.core

import android.content.Context
import technology.cariad.entertainment.metadataservice.common.codingprovider.IMediaCodingProviderListener
import technology.cariad.entertainment.metadataservice.common.codingprovider.service.IStorageConnection
import technology.cariad.entertainment.metadataservice.common.codingprovider.service.IStorageConnectionObserver
import technology.cariad.entertainment.metadataservice.common.codingprovider.service.StorageConnectionImpl

internal class CodingManager(private val context: Context) : IStorageConnectionObserver {

    private var parser = CodingParser()
    private var storageConnection: IStorageConnection? = null

    @Synchronized
    fun start(listener: IMediaCodingProviderListener): Boolean =
        if (parser.listener == null) {
            parser.listener = listener
            storageConnection = StorageConnectionImpl(context)
            storageConnection?.start(this) ?: false
        } else true

    @Synchronized
    fun stop() {
        storageConnection?.let {
            it.stop()
        }
        storageConnection = null
        parser.listener = null
    }

    /**
     * Implementation of interface StorageConnectionObserver
     */
    @Synchronized
    override fun onEstablished() {

        parser.listener?.let {
            it.onConnectionEstablished()
        }

        storageConnection?.let {
            parser.requestBlobs(it)
        }
    }

    @Synchronized
    override fun onDisconnected() {
        parser.listener?.let {
            it.onConnectionDisconnected()
        }
    }

    @Synchronized
    override fun onError() {
        parser.listener?.let {
            it.onConnectionError()
        }
    }

    override fun blobSubscribed(blobID: Int, blobValue: ShortArray) {

    }

    @Synchronized
    override fun blobRequested(blobID: Int, blobValue: ShortArray) {
        parser.parseBlob(blobID, blobValue)
    }

    override fun resultBlobSet(blobID: Int, success: Boolean) {

    }
}

