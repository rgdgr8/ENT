package de.esolutions.metadataserviceclient.api

/**
 * Callback interface used to answer requests for additional music metadata.
 */
interface IMusicMetadataCallback {

    /**
     * Provides the requested metadata.
     * If the content is null, the included error contains additional failure information.
     * @param musicMetadata The metadata.
     */
    fun onSuccess(musicMetadata: MusicMetadata)

    /**
     * Propagates an error to the client.
     * There are no known error conditions a client would have to react to at runtime.
     * It is not meaningful to attempt any retries with the same input data after receiving this
     * call.
     * @param message String describing the error. Mostly meant for logging and error analysis.
     */
    fun onError(message: String)
}
