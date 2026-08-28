package technology.cariad.entertainment.metadataservice.model

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataserviceclient.api.SongLyrics
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable

/**
 * A simple class providing song lyrics from a given MetadataService instance.
 * Clients of this class can register for changes of the cache via [Observable.addObserver].
 * Getter is provided for to retrieve the currently cached lyrics information.
 * @param serviceHolder An already connected and registered service instance.
 */
class SongLyricsModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()

    private var songLyrics: SongLyrics? = null

    private var request: Disposable? = null

    /**
     * Request to start observing lyrics
     * Display cached [SongLyrics] object fields.
     */
    fun startSongLyrics(lyricsType: Int, familyFriendly: Boolean) {

        serviceHolder.instance?.let { service ->
            val observable = service.observeSongLyrics(lyricsType, familyFriendly)
            log.info(this::startSongLyrics.name) { "observeSongLyrics" }

            songLyrics = null

            //open request
            request = observable.subscribe({ lyrics ->
                log.info(this::startSongLyrics.name) { "onNext: lyrics received" }

                songLyrics = lyrics
                setChanged()
                notifyObservers(songLyrics)

            }, {
                log.info(this::startSongLyrics.name) { "onError: error received: ${it.message}" }
            }, {
                log.info(this::startSongLyrics.name) { "onComplete: request completed" }
                setChanged()
                notifyObservers(null)
            })
        }
    }

    /**
     * Request to stop observing lyrics.
     * Clean the UI displaying cached and cleaned [SongLyrics] fields.
     */
    fun stopSongLyrics() {
        songLyrics = null
        setChanged()
        notifyObservers(songLyrics)
        request?.dispose()

        log.info(this::stopSongLyrics.name) { "unsubscribe lyrics" }
    }

    /**
     * Retrieve song lyrics.
     * @return A [SongLyrics] object containing the current lyrics
     */
    fun getSongLyrics(): SongLyrics? {
        return songLyrics
    }

    /**
     * Reset the actual song lyrics cached
     */
    fun reset() {
        songLyrics = null
        request = null

        setChanged()
        notifyObservers(null)
    }
}

