package technology.cariad.entertainment.metadataservice.model

import android.net.Uri
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable

class CoverArtLookupModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()

    // Received Cover Art Uri string cache
    private var coverArtUri: String? = null

    private var request: Disposable? = null

    /**
     * Request to search for the requested Cover Art image.
     * Display Cover Art image with returned corresponding [Uri].
     */
    fun searchForCoverArt(coverArtRequestData: CoverArtRequestData) {
        serviceHolder.instance?.let { service ->
            val observable = service.getCoverArt(coverArtRequestData)
            log.info(this::searchForCoverArt.name) { "getCoverArt is triggered" }

            //open request
            request = observable.subscribe({ uri ->
                log.info(this::searchForCoverArt.name) { "onNext: cover art image uri is received" }

                coverArtUri = uri
                setChanged()
                notifyObservers(uri)

            }, {
                log.info(this::searchForCoverArt.name) { "onError: error received: ${it.message}" }
            }, {
                log.info(this::searchForCoverArt.name) { "onComplete: request completed" }
            })
        }
    }

    /**
     * Dispose the request.
     * Clean the UI displaying returned cover art image.
     */
    fun disposeCoverArtLookupRequest() {
        coverArtUri = ""
        request?.let {
            setChanged()
            notifyObservers(coverArtUri)
            it.dispose()
        }

        log.info(this::disposeCoverArtLookupRequest.name) {
            "cover art lookup request is disposed"
        }
    }

    /**
     * Retrieve currently cached Cover Art image Uri.
     * @return An [Uri] for the found Cover Art image.
     */
    fun getCoverArtImageUri(): String? {
        return coverArtUri
    }

    /**
     * Resets the cover art Uri
     */
    fun reset() {
        request = null
        coverArtUri = null

        setChanged()
        notifyObservers(null)
    }
}

