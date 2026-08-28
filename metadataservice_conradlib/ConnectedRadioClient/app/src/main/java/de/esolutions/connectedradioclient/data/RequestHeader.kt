package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.api.SystemPropertyReader
import okhttp3.Headers

data class RequestHeader(val token: String, val requestID: String) {

    var mcc: String? = null
    var accept: String? = null
    var ifNoneMatch: String? = null
    var secondsSinceLastRegistration: Long? = null

    /**
     * Converts the header values to an okhttp3.Headers instance.
     */
    fun toHeaders(): Headers {
        val headers = Headers.Builder()

        headers.add("Authorization", "Bearer $token")
        headers.add(REQUEST_ID, requestID)
        headers.add(REQUEST_BRAND, SystemPropertyReader.getBrand())
        headers.add(REQUEST_PRODUCT, SystemPropertyReader.getProduct())
        headers.add(REQUEST_CLUSTER, SystemPropertyReader.getCluster())

        mcc?.let {
            headers.add("X-Mcc", it)
        }

        accept?.let {
            headers.add("Accept", it)
        }

        ifNoneMatch?.let {
            headers.add("If-None-Match", it)
        }

        secondsSinceLastRegistration?.let {
            headers.add("X-Registration-Age", it.toString())
        }

        return headers.build()
    }
}