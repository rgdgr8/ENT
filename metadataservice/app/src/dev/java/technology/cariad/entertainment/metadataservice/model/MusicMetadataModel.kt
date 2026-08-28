package technology.cariad.entertainment.metadataservice.model

import android.net.Uri
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataserviceclient.api.MusicMetadata
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable

class MusicMetadataModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()

    // Received Cover Art Uri string cache
    private var recordingByNameMetadata: MusicMetadata? = null
    private var recordingMetadata: MusicMetadata? = null
    private var albumMetadata: MusicMetadata? = null
    private var artistMetadata: MusicMetadata? = null

    private var recordingByNameRequest: Disposable? = null
    private var recordingRequest: Disposable? = null
    private var albumRequest: Disposable? = null
    private var artistRequest: Disposable? = null

    /**
     * Request to search for the requested Cover Art image.
     * Display Cover Art image with returned corresponding [Uri].
     */
    fun requestRecordingMetadataByName(recordingName: String, artistName: String) {
        serviceHolder.instance?.let { service ->

            log.info(this::getRecordingMetadataByName.name) {
                "recordingName=$recordingName artistName=$artistName"
            }

            val observable = service.getRecordingMetadataByName(recordingName, artistName)
            //open request
            recordingByNameRequest = observable.subscribe({ metadata ->
                log.info(this::getRecordingMetadataByName.name) { "received metadata=$metadata" }

                recordingByNameMetadata = metadata
                setChanged()
                notifyObservers()

            }, {
                log.info(this::getRecordingMetadataByName.name) { "onError: error received: ${it.message}" }
            })
        }
    }

    /**
     * Requests the recording of the metadata with the provided recording id
     * @param recordingId the recording id
     */
    fun requestRecordingMetadata(recordingId: String) {
        serviceHolder.instance?.let { service ->

            log.info(this::requestRecordingMetadata.name) {
                "recordingId=$recordingId"
            }

            val observable = service.getRecordingMetadata(recordingId)
            //open request
            recordingRequest = observable.subscribe({ metadata ->
                log.info(this::requestRecordingMetadata.name) { "received metadata=$metadata" }

                recordingByNameMetadata = metadata
                setChanged()
                notifyObservers()

            }, {
                log.info(this::requestRecordingMetadata.name) { "onError: error received: ${it.message}" }
            })
        }
    }

    /**
     * Request the metadata of the album of the provided album id
     * @param albumId the album id
     */
    fun requestAlbumMetadata(albumId: String) {
        serviceHolder.instance?.let { service ->

            log.info(this::requestAlbumMetadata.name) {
                "albumId=$albumId"
            }

            val observable = service.getAlbumMetadata(albumId)
            //open request
            albumRequest = observable.subscribe({ metadata ->
                log.info(this::requestAlbumMetadata.name) { "received metadata=$metadata" }

                recordingByNameMetadata = metadata
                setChanged()
                notifyObservers()

            }, {
                log.info(this::requestAlbumMetadata.name) { "onError: error received: ${it.message}" }
            })
        }
    }

    /**
     * Request the metadata of the artist of the provided artist id
     * @param artistId the artist id
     */
    fun requestArtistMetadata(artistId: String) {
        serviceHolder.instance?.let { service ->

            log.info(this::getRecordingMetadataByName.name) {
                "artistId=$artistId"
            }

            val observable = service.getArtistMetadata(artistId)
            //open request
            artistRequest = observable.subscribe({ metadata ->
                log.info(this::getRecordingMetadataByName.name) { "received metadata=$metadata" }

                recordingByNameMetadata = metadata
                setChanged()
                notifyObservers()

            }, {
                log.info(this::getRecordingMetadataByName.name) { "onError: error received: ${it.message}" }
            })
        }
    }

    /**
     * Dispose the request.
     * Clean the UI displaying returned cover art image.
     */
    fun disposeRequests() {
        recordingByNameRequest?.dispose()
        recordingRequest?.dispose()
        albumRequest?.dispose()
        artistRequest?.dispose()

        reset()

        log.info(this::disposeRequests.name) {
            "requests disposed"
        }
    }

    /**
     * Retrieve currently cached Cover Art image Uri.
     * @return An [Uri] for the found Cover Art image.
     */
    fun getRecordingMetadataByName(): MusicMetadata? {
        return recordingByNameMetadata
    }

    /**
     * Retrieve currently cached Cover Art image Uri.
     * @return An [Uri] for the found Cover Art image.
     */
    fun getRecordingMetadata(): MusicMetadata? {
        return recordingMetadata
    }

    /**
     * Retrieve currently cached Cover Art image Uri for the album.
     * @return An [Uri] for the found Cover Art image.
     */
    fun getAlbumMetadata(): MusicMetadata? {
        return albumMetadata
    }

    /**
     * Retrieve currently cached Cover Art image Uri for the artist.
     * @return An [Uri] for the found Cover Art image.
     */
    fun getArtistMetadata(): MusicMetadata? {
        return artistMetadata
    }

    /**
     * Resets all the metadata cached
     */
    fun reset() {
        recordingByNameMetadata = null
        recordingMetadata = null
        albumMetadata = null
        artistMetadata = null

        recordingByNameRequest = null
        recordingRequest = null
        albumRequest = null
        artistRequest = null

        setChanged()
        notifyObservers()
    }
}

