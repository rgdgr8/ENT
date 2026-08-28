package de.esolutions.metadataserviceclient.api

/**
 * Callback interface used to answer requests changing the current online lookup state.
 */
interface IOnlineLookupStateCallback {

    /**
     * Notifies a client of the current online lookup state.
     * The new state may not match the requested state if online lookup is disabled for other
     * reasons.
     * @param enabled The new state of the online lookup after the request was processed.
     */
    fun onSuccess(enabled: Boolean)

    /**
     * Propagates an error to the client.
     * There are no known error conditions a client would have to react to at runtime.
     * It is not meaningful to attempt any retries with the same input data after receiving this
     * call.
     * @param message String describing the error. Mostly meant for logging and error analysis.
     */
    fun onError(message: String)
}
