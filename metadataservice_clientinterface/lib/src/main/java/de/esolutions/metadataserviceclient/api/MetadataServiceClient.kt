package de.esolutions.metadataserviceclient.api

import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.IInterface
import android.os.Looper
import java.util.concurrent.atomic.AtomicInteger

/**
 * For usage refer to [IMetadataServiceClient]
 *
 * Disclaimer:
 * The delivery of the client interface of the Metadata-Service App does not include any transitive
 * dependencies in order to use it within an Android application. These dependencies must be taken
 * into account by the client using the client interface, especially with respect to Open-Source
 * compliance, and must be handled according to the applicable Open-Source source process in the
 * project.
 */
class MetadataServiceClient(
    private val aidl: IMetadataServiceAidl,
    private val handler: Handler
) : IMetadataServiceClient {

    private val binder = Binder()
    private val clientId: Int = aidl.management().registerClient(toString(), binder)
    private var requestIdCounter: AtomicInteger = AtomicInteger(0)
    private val requests: MutableMap<Int, IInterface> = mutableMapOf()

    override fun unregisterClient() {
        handler.post {
            requests.clear()
            aidl.management().unregisterClient(clientId, toString())
        }
    }

    override fun changeOnlineLookupStateSubscribe(
        enabled: Boolean,
        callback: IOnlineLookupStateCallback
    ): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : BooleanSingleCallback.Stub() {

                override fun onSuccess(bool: Boolean) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onSuccess(bool)
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.changeOnlineLookupStateSubscribe(clientId, requestId, enabled, callbackAidl)
        }
        return requestId
    }

    override fun changeOnlineLookupStateDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.changeOnlineLookupStateDispose(clientId, requestId)
            }
        }
    }

    override fun observeSongRecognitionSubscribe(callback: ISongRecognitionCallback): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : RecognizedSongInfoCallback.Stub() {

                override fun onNext(songInfo: RecognizedSongInfo) {
                    handler.post {
                        if (requests.contains(requestId)) {
                            callback.onNext(songInfo)
                        }
                    }
                }

                override fun onComplete() {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onComplete()
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.observeSongRecognitionSubscribe(clientId, requestId, callbackAidl)
        }
        return requestId
    }

    override fun observeSongRecognitionDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.observeSongRecognitionDispose(clientId, requestId)
            }
        }
    }

    override fun getCoverArtSubscribe(
        requestData: CoverArtRequestData,
        callback: ICoverArtCallback
    ): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : StringCallback.Stub() {

                override fun onNext(uri: String) {
                    handler.post {
                        if (requests.contains(requestId)) {
                            callback.onNext(uri)
                        }
                    }
                }

                override fun onComplete() {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onComplete()
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.getCoverArtSubscribe(clientId, requestId, requestData, callbackAidl)
        }
        return requestId
    }

    override fun getCoverArtDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.getCoverArtDispose(clientId, requestId)
            }
        }
    }

    override fun getPermissionDataSubscribe(callback: IPermissionDataSingleCallback): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : PermissionDataSingleCallback.Stub() {

                override fun onSuccess(permissionData: PermissionData) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onSuccess(permissionData)
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.getPermissionDataSubscribe(clientId, requestId, callbackAidl)
        }
        return requestId
    }

    override fun getPermissionDataDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.getPermissionDataDispose(clientId, requestId)
            }
        }
    }

    override fun observeSongLyricsSubscribe(
        type: Int,
        familyFriendly: Boolean,
        callback: ISongLyricsCallback
    ): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : SongLyricsCallback.Stub() {

                override fun onNext(songLyrics: SongLyrics) {
                    handler.post {
                        if (requests.contains(requestId)) {
                            callback.onNext(songLyrics)
                        }
                    }
                }

                override fun onComplete() {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onComplete()
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.observeSongLyricsSubscribe(clientId, requestId, type, familyFriendly, callbackAidl)
        }
        return requestId
    }

    override fun observeSongLyricsDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.observeSongLyricsDispose(clientId, requestId)
            }
        }
    }

    override fun getArtistMetadataSubscribe(
        artistId: String,
        callback: IMusicMetadataCallback
    ): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : MusicMetadataSingleCallback.Stub() {

                override fun onSuccess(musicMetadata: MusicMetadata) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onSuccess(musicMetadata)
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.getArtistMetadataSubscribe(clientId, requestId, artistId, callbackAidl)
        }
        return requestId
    }

    override fun getArtistMetadataDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.getArtistMetadataDispose(clientId, requestId)
            }
        }
    }

    override fun getAlbumMetadataSubscribe(albumId: String, callback: IMusicMetadataCallback): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : MusicMetadataSingleCallback.Stub() {

                override fun onSuccess(musicMetadata: MusicMetadata) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onSuccess(musicMetadata)
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.getAlbumMetadataSubscribe(clientId, requestId, albumId, callbackAidl)
        }
        return requestId
    }

    override fun getAlbumMetadataDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.getAlbumMetadataDispose(clientId, requestId)
            }
        }
    }

    override fun getRecordingMetadataSubscribe(
        recordingId: String,
        callback: IMusicMetadataCallback
    ): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : MusicMetadataSingleCallback.Stub() {

                override fun onSuccess(musicMetadata: MusicMetadata) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onSuccess(musicMetadata)
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.getRecordingMetadataSubscribe(clientId, requestId, recordingId, callbackAidl)
        }
        return requestId
    }

    override fun getRecordingMetadataDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.getRecordingMetadataDispose(clientId, requestId)
            }
        }
    }

    override fun getRecordingMetadataByNameSubscribe(
        recordingName: String,
        artistName: String,
        callback: IMusicMetadataCallback
    ): Int {
        val requestId = requestIdCounter.getAndIncrement()
        handler.post {
            val callbackAidl = object : MusicMetadataSingleCallback.Stub() {

                override fun onSuccess(musicMetadata: MusicMetadata) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onSuccess(musicMetadata)
                        }
                    }
                }

                override fun onError(message: String) {
                    handler.post {
                        if (requests.remove(requestId) != null) {
                            callback.onError(message)
                        }
                    }
                }
            }
            requests[requestId] = callbackAidl
            aidl.getRecordingMetadataByNameSubscribe(
                clientId, requestId, recordingName, artistName, callbackAidl
            )
        }
        return requestId
    }

    override fun getRecordingMetadataByNameDispose(requestId: Int) {
        handler.post {
            if (requests.remove(requestId) != null) {
                aidl.getRecordingMetadataByNameDispose(clientId, requestId)
            }
        }
    }

    companion object {

        /**
         * Converts IBinder to [IMetadataServiceClient] interface.
         * @param binder IBinder instance passed to the client through
         * [android.content.ServiceConnection.onServiceConnected]
         * @param handler Optional handler requests are dispatched to, if no handler passed,
         * requests are handled on the application's main thread.
         * @return [IMetadataServiceClient]
         */
        fun fromBinder(binder: IBinder, handler: Handler = Handler(Looper.getMainLooper())):
                IMetadataServiceClient =
            MetadataServiceClient(IMetadataServiceAidl.Stub.asInterface(binder), handler)
    }
}
