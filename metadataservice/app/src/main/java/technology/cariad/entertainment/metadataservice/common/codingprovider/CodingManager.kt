package technology.cariad.entertainment.metadataservice.common.codingprovider

import android.content.Context

class CodingManager(private val context: Context) : IMediaCodingProviderListener {

    private val codingProvider = MediaCodingProvider(context)

    private var onInit: () -> Unit = {}

    /**
     * Initializes this class. The passed callback is invoked once initialization has finished.
     * @param onInitialized
     */
    fun initialize(onInitialized: () -> Unit) {
        onInit = onInitialized

        codingProvider.start(this)
    }

    /**
     * De-initializes this class.
     */
    fun deinitialize() {
        codingProvider.stop()
    }

    /**
     * @see [IMediaCodingProviderListener.onConnectionEstablished]
     */
    override fun onConnectionEstablished() {
    }

    /**
     * @see [IMediaCodingProviderListener.onConnectionDisconnected]
     */
    override fun onConnectionDisconnected() {
    }

    /**
     * @see [IMediaCodingProviderListener.onConnectionError]
     */
    override fun onConnectionError() {

    }

    /**
     * @see [IMediaCodingProviderListener.onCodingBlobAvailable]
     */
    override fun onCodingBlobAvailable(blobID: Int, shortArray: ShortArray) {

    }

    /**
     * @see [IMediaCodingProviderListener.onCountryOfSalesAvailable]
     */
    override fun onCountryOfSalesAvailable() {
        onInit()
    }
}

