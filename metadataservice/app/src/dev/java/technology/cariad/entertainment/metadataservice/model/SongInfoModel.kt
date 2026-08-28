package technology.cariad.entertainment.metadataservice.model

import android.os.Handler
import android.os.Looper
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable

/**
 * A simple class providing station data from a given MetadataService instance.
 * Song recognition from GN service can be started and the result is cached internally.
 * Clients of this class can register for changes of the cache via [Observable.addObserver].
 * Getter is provided for to retrieve the currently cached song information.
 * @param serviceHolder An already connected and registered service instance.
 */
class SongInfoModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()
    private val mainHandler = Handler(Looper.getMainLooper())

    //songInfo cache
    private var songInfo: RecognizedSongInfo? = null

    private var request: Disposable? = null

    /**
     * Request to start the song recognition.
     * Display cached [RecognizedSongInfo] object fields.
     */
    fun startSongRecognition() {

        serviceHolder.instance?.let { service ->
            val observable = service.observeSongRecognition()
            log.info(this::startSongRecognition.name) { "observeSongRecognition" }

            songInfo = null

            //open request
            request = observable.subscribe({ song ->
                log.info(this::startSongRecognition.name) { "onNext: songInfo received" }

                songInfo = song
                notifyObserversOnMain(songInfo)

            }, {
                log.info(this::startSongRecognition.name) { "onError: error received: ${it.message}" }
            }, {
                log.info(this::startSongRecognition.name) { "onComplete: request completed" }
                notifyObserversOnMain(null)
            })
        }
    }

    /**
     * Request to stop the song recognition.
     * Clean the UI displaying cached and cleaned [RecognizedSongInfo] fields.
     */
    fun stopSongRecognition() {
        songInfo = null
        notifyObserversOnMain(songInfo)
        request?.dispose()

        log.info(this::stopSongRecognition.name) { "song recognition is stopped" }
    }

    /**
     * Retrieve currently cached song information.
     * @return A [RecognizedSongInfo] object containing currently recognized song information.
     */
    fun getSongInfo(): RecognizedSongInfo? {
        return songInfo
    }

    /**
     * Resets the current song info cached
     */
    fun reset() {
        songInfo = null
        request = null

        notifyObserversOnMain(null)
    }

    private fun notifyObserversOnMain(value: RecognizedSongInfo?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setChanged()
            notifyObservers(value)
            return
        }

        mainHandler.post {
            setChanged()
            notifyObservers(value)
        }
    }
}
