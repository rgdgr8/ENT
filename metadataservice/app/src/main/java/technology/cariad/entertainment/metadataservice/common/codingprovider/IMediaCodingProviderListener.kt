package technology.cariad.entertainment.metadataservice.common.codingprovider

/**
 * To notify the customer that the data or connection is available.
 * Must be implemented by the customer
 */
interface IMediaCodingProviderListener {

    /**
     * Connection has been established
     */
    fun onConnectionEstablished()

    /**
     * Connection has been disabled
     */
    fun onConnectionDisconnected()

    /**
     * Connection has been disabled
     */
    fun onConnectionError()

    /**
     * Connection has been established
     */
    fun onCodingBlobAvailable(blobID: Int, shortArray: ShortArray)

    /**
     * Wlan availability coding is successfully available
     */
    fun onCountryOfSalesAvailable()
}
