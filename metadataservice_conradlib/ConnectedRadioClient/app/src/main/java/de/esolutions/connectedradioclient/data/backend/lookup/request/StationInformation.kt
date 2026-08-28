package de.esolutions.connectedradioclient.data.backend.lookup.request

import com.squareup.moshi.Json

data class StationInformation(
    @Json(name = "etags") val eTags: ETagRequest,
)