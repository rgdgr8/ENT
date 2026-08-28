package de.esolutions.connectedradioclient.data.extensions

import de.esolutions.connectedradioclient.data.REQUEST_ID
import okhttp3.Request

/**
 * Returns the request id from the request or null if not set.
 */
fun Request.getRequestID(): String? {
    return this.header(REQUEST_ID)
}