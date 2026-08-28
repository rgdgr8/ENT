package de.esolutions.connectedradioclient.data.backend.lookup.result

import com.squareup.moshi.Json

data class ETagResult(
    @Json(name = "metadata") val metadataETag: String?,
    @Json(name = "bearers") val bearersETag: String?,
    @Json(name = "streams") val streamsEtag: String?,
)