package de.esolutions.magicadapter.stapi

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.magicadapter.stapi.ServiceConnectionCallback
 */
interface ServiceConnectionCallback {
    fun serviceConnected()
    fun serviceDisconnected(failureReason: ServiceConnectionFailure?)
}
