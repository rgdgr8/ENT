package technology.cariad.entertainment.metadataservice.common.codingprovider.service

internal interface IStorageConnectionObserver {

    /**
     * Callback to inform connection is now established
     */
    fun onEstablished()

    /**
     * Callback to inform connection is now disconnected
     */
    fun onDisconnected()

    /**
     * Callback to inform connection is now error
     */
    fun onError()

    /**
     * Update with blob data for the request subscribeBlob identified with blobID
     */
    fun blobSubscribed(blobID: Int, blobValue: ShortArray)

    /**
     * Response with blob data for the request getBlob identified with blobID
     */
    fun blobRequested(blobID: Int, blobValue: ShortArray)

    /**
     * Response to the request setBlob identified with blobID
     */
    fun resultBlobSet(blobID: Int, success: Boolean)
}
