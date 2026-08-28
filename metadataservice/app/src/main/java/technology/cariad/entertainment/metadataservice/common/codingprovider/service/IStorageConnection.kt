package technology.cariad.entertainment.metadataservice.common.codingprovider.service

internal interface IStorageConnection {
    /**
     * Call to start connection with asi.diag.pers.IStorageService
     */
    fun start(observer: IStorageConnectionObserver): Boolean

    /**
     * Call to stop connection with asi.diag.pers.IStorageService
     */
    fun stop()

    /**
     * Call to Subscribe for Blob identified blobID
     * Note: This will applicable connection is successfully started
     *
     * return
     *  true: success
     *  false: failed due to any reason
     *
     *  Response will be provided asynchronously via StorageConnectionObserver
     */
    fun subscribeBlob(blobID: Int): Boolean

    /**
     * Call to Get a Blob identified blobID
     * Note: This will applicable connection is successfully started
     *
     * return
     *  true: success
     *  false: failed due to any reason
     *
     *  Response will be provided asynchronously via StorageConnectionObserver
     */
    fun getBlob(blobID: Int): Boolean

    /**
     * Call to Set a Blob identified blobID
     * Note: This will applicable connection is successfully started
     *
     * return
     *  true: requested successfully
     *  false: failed due to any reason
     *
     * Response will be provided asynchronously via StorageConnectionObserver
     */
    fun setBlob(blobID: Int, blobValue: ShortArray): Boolean
}
