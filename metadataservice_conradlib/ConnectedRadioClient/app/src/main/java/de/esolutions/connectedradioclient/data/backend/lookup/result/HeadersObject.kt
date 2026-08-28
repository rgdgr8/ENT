package de.esolutions.connectedradioclient.data.backend.lookup.result

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HeadersObject(
    @Json(name = "Retry-After") val retryAfter: Long
)