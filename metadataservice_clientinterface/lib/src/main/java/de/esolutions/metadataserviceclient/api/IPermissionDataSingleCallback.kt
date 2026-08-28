package de.esolutions.metadataserviceclient.api

/**
 * Callback interface used to answer clients about the latest permissions state
 */
interface IPermissionDataSingleCallback {

    /**
     * Notifies the client about the current permissions state e.g list of the missing permissions
     * @param permissionData Current state of the permissions
     */
    fun onSuccess(permissionData: PermissionData)

    /**
     * Propagates an error to the client.
     * There are no known error conditions a client would have to react to at runtime.
     * @param message String describing the error. Mostly meant for logging and error analysis.
     */
    fun onError(message: String)
}
