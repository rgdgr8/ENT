package de.esolutions.connectedradioclient.data.backend.lookup.result

import com.squareup.moshi.Json

data class LookupResult(
    val status: Int,
    val data: DataObject?,
    val headers: HeadersObject?,
    @Json(name = "etags") val eTags: ETagResult?
)