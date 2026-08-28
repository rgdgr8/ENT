package de.esolutions.metadataserviceclient.api

/**
 * Client interface of the metadata service.
 *
 * Usage:
 * The client app implements an [android.content.ServiceConnection] and binds to the
 * service using [android.content.Context.bindService]. The binder interface passed to the client
 * via [android.content.ServiceConnection.onServiceConnected] can be converted into a type
 * implementing the [IMetadataServiceClient] interface using [MetadataServiceClient.fromBinder].
 *
 * Upon construction of the [MetadataServiceClient] it registers itself with the service.
 * Before unbinding the service a client should dispose any running requests and unregister itself
 * using [unregisterClient].
 *
 * The interface is designed to be easily integrated with reactive streams.
 * Data is requested from the service using the provided subscribe calls, passing in request
 * parameters and a callback object to receive asynchronous responses. Depending on the request a
 * single result value or multiple successive results are provided. Receiving an update in the
 * later case usually invalidates any previous results.
 * Multiple requests can be issued simultaneously. Each subscribe call returns the id of the new
 * request. This id is required to cancel a request using the corresponding dispose function.
 * If a client disposed a request no further callbacks are issued. Once a request is fulfilled i.e.
 * onSuccess, onComplete or onError has been called, it is not necessary to call dispose anymore.
 * For further details on the callbacks refer to the documentation of the respective interfaces.
 *
 * Threading:
 * By default all requests and async responses of a [MetadataServiceClient] obtained via
 * [MetadataServiceClient.fromBinder] are dispatched on the main thread.
 * This behavior can be overwritten by passing an [android.os.Handler] associated with a different
 * thread as second parameter to [MetadataServiceClient.fromBinder].
 * This synchronization allows to avoid race conditions, where e.g. callbacks would be triggered
 * after a previous dispose call. Additionally it makes the client interface safe for usage
 * from multiple threads.
 *
 * Exceptions:
 * If the underlying implementation is based on Android's AIDL/Binder IPC, exceptions from type
 * [android.os.RemoteException] or derived thereof should be expected and handled for each service
 * call.
 *
 * Disclaimer:
 * The delivery of the client interface of the Metadata-Service App does not include any transitive
 * dependencies in order to use it within an Android application. These dependencies must be taken
 * into account by the client using the client interface, especially with respect to Open-Source
 * compliance, and must be handled according to the applicable Open-Source source process in the
 * project.
 */
interface IMetadataServiceClient {

    /**
     * Signals to the service that it is no longer being used by this client.
     * Any open requests should be disposed before calling this function.
     * Should be called before unbinding from the service.
     */
    fun unregisterClient()

    /**
     * Attempts to change the online lookup state to the given value of [enabled].
     *
     * @param enabled True if online lookup should be enabled.
     * @param callback Callback object to receive the result:
     * @return The id of the request as [Int].
     * Can be used with the corresponding [changeOnlineLookupStateDispose] function to cancel the
     * request.
     */
    fun changeOnlineLookupStateSubscribe(
        enabled: Boolean,
        callback: IOnlineLookupStateCallback
    ): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [changeOnlineLookupStateSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun changeOnlineLookupStateDispose(requestId: Int)

    /**
     * Observes the results of the song recognition.
     *
     * Runs as long as there is at least one client subscribed. As soon as all requests are disposed
     * the song recognition is free to stop itself.
     *
     * @param callback Callback object to receive the results:
     * @return The id of the request as [Int].
     * Can be used with the corresponding [observeSongRecognitionDispose] function to cancel the
     * request.
     */
    fun observeSongRecognitionSubscribe(callback: ISongRecognitionCallback): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [observeSongRecognitionSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun observeSongRecognitionDispose(requestId: Int)

    /**
     * Query a cover art image.
     *
     * @param requestData Specifies which cover art image should be fetched.
     * @param callback delivering the result of the operation:
     * @return The id of the request as [Int].
     * Can be used with the corresponding [getCoverArtDispose] function to cancel the request.
     */
    fun getCoverArtSubscribe(requestData: CoverArtRequestData, callback: ICoverArtCallback): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [getCoverArtSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun getCoverArtDispose(requestId: Int)

    /**
     * Query current permissions state.
     *
     * @param callback delivering the result of the operation:
     * @return The id of the request as [Int].
     * Can be used with the corresponding [getPermissionDataDispose] function to cancel the request.
     */
    fun getPermissionDataSubscribe(callback: IPermissionDataSingleCallback): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [getPermissionDataSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun getPermissionDataDispose(requestId: Int)

    /**
     * Requests the lyrics for the currently playing track.
     *
     * @param type The type of lyrics that should be provided.
     * @param familyFriendly Indicates if the family friendly version of the song lyrics should be
     *                       provided.
     * @param callback Callback object to receive the results.
     * @return The id of the request as [Int].
     * Can be used with the corresponding [observeSongLyricsDispose] function to cancel the request.
     */
    fun observeSongLyricsSubscribe(
        type: Int,
        familyFriendly: Boolean,
        callback: ISongLyricsCallback
    ): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [observeSongLyricsSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun observeSongLyricsDispose(requestId: Int)


    /**
     * Requests metadata for the artist with the given id.
     *
     * @param artistId The id of the artist.
     * @return The id of the request as [Int].
     * Can be used with the corresponding [getArtistMetadataDispose] function to cancel the request.
     */
    fun getArtistMetadataSubscribe(artistId: String, callback: IMusicMetadataCallback): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [getArtistMetadataSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun getArtistMetadataDispose(requestId: Int)

    /**
     * Requests metadata for the album with the given id.
     *
     * @param albumId The id of the album.
     * @return The id of the request as [Int].
     * Can be used with the corresponding [getAlbumMetadataDispose] function to cancel the request.
     */
    fun getAlbumMetadataSubscribe(albumId: String, callback: IMusicMetadataCallback): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [getAlbumMetadataSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun getAlbumMetadataDispose(requestId: Int)

    /**
     * Requests metadata for a specific recording.
     *
     * @param recordingId The id of the recording.
     * @return The id of the request as [Int].
     * Can be used with the corresponding [getRecordingMetadataDispose]
     * function to cancel the request.
     */
    fun getRecordingMetadataSubscribe(recordingId: String, callback: IMusicMetadataCallback): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [getRecordingMetadataSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun getRecordingMetadataDispose(requestId: Int)

    /**
     * Requests metadata for a specific recording by artist and album name.
     *
     * @param recordingName The name of the recording.
     * @param artistName The name of the artist.
     * @return The id of the request as [Int].
     * Can be used with the corresponding [getRecordingMetadataByNameDispose]
     * function to cancel the request.
     */
    fun getRecordingMetadataByNameSubscribe(
        recordingName: String,
        artistName: String, callback: IMusicMetadataCallback
    ): Int

    /**
     * Indicate that a caller is no longer interested in the result of a previous
     * [getRecordingMetadataByNameSubscribe] call.
     *
     * @param requestId The id of the request to dispose.
     */
    fun getRecordingMetadataByNameDispose(requestId: Int)
}
