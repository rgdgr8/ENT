package de.esolutions.metadataserviceclient.api

/**
 * Callback interface used to answer cover art requests.
 */
interface ICoverArtCallback {

    /**
     * Notifies the client of an available cover art.
     * @param uri String representation of an [android.net.Uri].
     * Permission to access the resource is only granted temporarily and does not persist across
     * device restarts.
     */
    fun onNext(uri: String)

    /**
     * Signals to the client that request processing has completed. No further results will be
     * provided via [onNext].
     * May be called without a previous [onNext] call if a request can't be fulfilled.
     */
    fun onComplete()

    /**
     * Propagates an error to the client.
     * Will be invoked if insufficient information was provided to the request.
     * It is not meaningful to attempt any retries with the same input data after receiving this
     * call.
     * @param message String describing the error. Mostly meant for logging and error analysis.
     */
    fun onError(message: String)
}
