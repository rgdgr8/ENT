package technology.cariad.entertainment.metadataserviceclient.api

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/*
 Hint to disable the IDE's auto formatting. The file contains lines exceeding the current maximum
 line length mandated by the coding guidelines. However, splitting them up causes the RPC2AIDL
 generator to produce erroneous output.
 */
//@formatter:off

/**
 * Disclaimer:
 * The delivery of the client inter-face of the Metadata-Service App does not include any transitive
 * dependencies in order to use it within an Android application. These dependencies must be taken
 * into account by the client using the client inter-face, especially with respect to Open-Source
 * compliance, and must be handled according to the applicable Open-Source source process in the
 * project.
 */
interface IMetadataService {

    /**
     * Attempts to change the online lookup state to the given value of [enabled].
     *
     * @param enabled True if online lookup should be enabled.
     * @return A [Single] delivering the result of the operation:
     *  - onSuccess(newState): The new state of the online lookup after the attempt was successfully
     *                         processed.
     *  - onError(RuntimeException): Should not be used.
     *
     * @note Subscribing to or disposing of the Single may throw an [android.os.RemoteException] or
     * exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun changeOnlineLookupState(enabled: Boolean): Single<Boolean>

    /**
     * Query for station data to the station identified by the given [StationBearer].
     *
     * If the caller already has some cached station data from a former call he will pass in the
     * [Station.lastModified] of that cached data as an additional argument. If there is no data
     * newer than the already cached data, then no new data has to be provided.
     *
     * @param bearer Identifies the station for which data should be queried.
     * @param cachedLastModified Tag identifying a previously provided set of station data. If no
     *                           such tag is available, an empty string should be passed.
     * @return An [Observable] delivering the results of the operation:
     *   - onNext(station): The result of fetching the data. Can also be sent multiple times if
     *                      fetching is done iteratively.
     *   - onComplete(): Signals that no further data will be delivered or that the already cached
     *                   data is up to date.
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in.
     *                                Other problems like for example fetch errors should be hidden
     *                                from caller and handled by service provider via retries.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    @Suppress("MaxLineLength") //newlines in function declarations break RPC2AIDL generator
    fun observeRadioStationData(bearer: StationBearer, cachedLastModified: String): Observable<Station>

    /**
     * Observes the results of the song recognition.
     *
     * It runs as long as there is at least one subscriber to the returned [Observable].
     * As soon as all subscribers are gone, the song recognition is free to stop itself.
     *
     * @return An [Observable] delivering the recognized songs:
     *   - onNext(recognizedSongInfo): Information about a new recognized song.
     *   - onComplete(): Should not be used. Recognition should only be stopped when there are no
     *                   active subscribers.
     *   - onError(RuntimeException): Should only be used in case of an unrecoverable error.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun observeSongRecognition(): Observable<RecognizedSongInfo>

    /**
     * Query a cover art image.
     *
     * @param requestData Specifies which cover art image should be fetched.
     * @return An [Observable] delivering the result of the operation:
     *   - onNext(String): The uri that can be used to obtain the cover arts binary data.
     *   - onComplete(): Signals that no further data will be delivered or if there was no former
     *                   onNext(), that no cover art is available for the provided
     *                   [CoverArtRequestData]
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in.
     *                                Other problems like for example fetch errors should be hidden
     *                                from caller and handled by service provider via retries.
     * @throws [android.os.RemoteException] If the underlying implementation is based on Android's
     * AIDL/Binder IPC, exceptions of this type or derived thereof should be expected and handled.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun getCoverArt(requestData: CoverArtRequestData): Observable<String>

    /**
     * Query current state of user permissions.
     *
     * @return A [Single] delivering permissions, that have not been granted.
     *   - onSuccess(PermissionData): Current state of user permissions.
     *   - onError(RuntimeException): Should not be used.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun getPermissionData(): Single<PermissionData>

    /**
     * Query for station data to the station identified by the given [StationBearerAsia].
     *
     * The caller should ensure that the permission
     * [PermissionData.PERMISSION_FINE_BACKGROUND_LOCATION] was granted by the user before calling
     * this function.
     *
     * @param bearer Identifies the station for which data should be queried.
     * @return A [Single] delivering the results of the operation:
     *   - onSuccess(StationAsia): The result of fetching the data. If no data is available, an
     *                             empty object is passed.
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in or
     *                                missing permissions. Other problems like for example fetch
     *                                errors should be hidden from caller and handled by service
     *                                provider via retries.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun observeRadioStationDataAsia(bearer: StationBearerAsia): Single<StationAsia>

    /**
     * Requests the lyrics for the currently playing track.
     *
     * @param type The type of lyrics that should be provided.
     * @param familyFriendly Indicates if the family friendly version of the song lyrics should be
     *                       provided.
     * @return An [Observable] delivering the result of the operation:
     *   - onNext(SongLyrics): Notifies the client that the lyrics for the currently playing song
     *                         are available.
     *   - onComplete(): Notifies the client that the lyrics are no longer valid and the request has
     *                   been completed. If there was no former onNext() no lyrics can be provided
     *                   for the current track.
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in.
     *                                Other problems like for example fetch errors should be hidden
     *                                from caller and handled by service provider via retries.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun observeSongLyrics(type: Int, familyFriendly: Boolean): Observable<SongLyrics>

    /**
     * Requests metadata for the artist with the given id.
     *
     * @param artistId The id of the artist.
     * @return A [Single] delivering the results of the operation:
     *   - onSuccess(MusicMetadata): The result of fetching the data.
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in or
     *                                missing permissions. Other problems like for example fetch
     *                                errors should be hidden from caller and handled by service
     *                                provider via retries.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun getArtistMetadata(artistId: String): Single<MusicMetadata>

    /**
     * Requests metadata for the album with the given id.
     *
     * @param albumId The id of the album.
     * @return A [Single] delivering the results of the operation:
     *   - onSuccess(MusicMetadata): The result of fetching the data.
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in or
     *                                missing permissions. Other problems like for example fetch
     *                                errors should be hidden from caller and handled by service
     *                                provider via retries.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun getAlbumMetadata(albumId: String): Single<MusicMetadata>

    /**
     * Requests metadata for a specific recording.
     *
     * @param recordingId The id of the recording.
     * @return A [Single] delivering the results of the operation:
     *   - onSuccess(MusicMetadata): The result of fetching the data.
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in or
     *                                missing permissions. Other problems like for example fetch
     *                                errors should be hidden from caller and handled by service
     *                                provider via retries.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun getRecordingMetadata(recordingId: String): Single<MusicMetadata>

    /**
     * Requests metadata for a specific recording by artist and album name.
     *
     * @param recordingName The name of the recording.
     * @param artistName The name of the artist.
     * @return A [Single] delivering the results of the operation:
     *   - onSuccess(MusicMetadata): The result of fetching the data.
     *   - onError(RuntimeException): Should only be used to signal illegal arguments passed in or
     *                                missing permissions. Other problems like for example fetch
     *                                errors should be hidden from caller and handled by service
     *                                provider via retries.
     *
     * @note Subscribing to or disposing of the Observable may throw an [android.os.RemoteException]
     * or exceptions derived thereof, if the underlying implementation is based on Android's
     * AIDL/Binder IPC.
     */
    fun getRecordingMetadataByName(recordingName: String, artistName: String): Single<MusicMetadata>
}

//@formatter:on
