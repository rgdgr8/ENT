package de.esolutions.metadataserviceclient.api

/**
 * Callback interface used to update clients with lyrics of the currently playing song.
 */
interface ISongLyricsCallback {

    /**
     * Notifies the client that the lyrics for the currently playing song are available.
     * @param lyrics The lyrics in the format requested by the client including the current
     *               play position.
     */
    fun onNext(songLyrics: SongLyrics)

    /**
     * Notifies the client that the lyrics are no longer valid and the request has completed.
     * This happens when a track change is detected, either as the track reached its end, or caused
     * by user input. If no lyrics for the current track can be provided, the request is completed
     * without error as well.
     */
    fun onComplete()

    /**
     * Propagates an error to the client.
     * There are no known error conditions a client would have to react to at runtime.
     * @param message String describing the error. Mostly meant for logging and error analysis.
     */
    fun onError(message: String)
}
