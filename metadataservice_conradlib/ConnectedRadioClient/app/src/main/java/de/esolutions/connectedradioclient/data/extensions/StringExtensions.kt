package de.esolutions.connectedradioclient.data.extensions

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * Replace the placeholder in the URL that is returned by the registration endpoint.
 */
fun String.replaceEsoUrlPlaceholder(search: String, replace: String): String {
    return this.replace("$($search)", replace)
}

/**
 * Uses a string to build a HttpUrl and appends the [relativeUrl] to it.
 * Builder will not encode the url parameter.
 *
 * @throws IllegalArgumentException If this is not a well-formed HTTP or HTTPS URL.
 */
fun String.buildUrl(relativeUrl: String): HttpUrl {
    return this.toHttpUrl()
        .newBuilder()
        .addEncodedPathSegments(relativeUrl.trimStart('/'))
        .build()
}