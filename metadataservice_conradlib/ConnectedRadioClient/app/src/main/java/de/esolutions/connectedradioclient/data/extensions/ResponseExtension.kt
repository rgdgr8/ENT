package de.esolutions.connectedradioclient.data.extensions

import okhttp3.Response

/**
 * Return Retry-After Header from Response as Long in seconds or null.
 * Only supports a delay in seconds as number. A date is not supported.
 */
fun Response.getRetryAfter(): Long? {
    return this.header("Retry-After")?.toLongOrNull()
}

/**
 * Returns the request id from the request or null if not set.
 */
fun Response.getRequestID(): String? {
    return this.request.getRequestID()
}