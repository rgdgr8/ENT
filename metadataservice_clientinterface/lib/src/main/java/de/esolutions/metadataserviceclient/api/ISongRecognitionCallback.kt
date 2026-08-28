package de.esolutions.metadataserviceclient.api

/**
 * Callback interface used to update clients with information about the currently playing song.
 */
interface ISongRecognitionCallback {

    /**
     * Notifies the client that a new song was recognized.
     * @param songInfo Metadata of the new song.
     */
    fun onNext(songInfo: RecognizedSongInfo)

    /**
     *  Signal to the client that song recognition has stopped.
     */
    fun onComplete()

    /**
     * Propagates an error to the client.
     * There are no known error conditions a client would have to react to at runtime.
     * @param message String describing the error. Mostly meant for logging and error analysis.
     */
    fun onError(message: String)
}
