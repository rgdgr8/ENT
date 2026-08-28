package de.esolutions.connectedradioclient.data.backend.lookup.request

import com.squareup.moshi.Json
import de.esolutions.connectedradioclient.data.ETags

data class ETagRequest(
    @Json(name = "metadata") val metadataETag: String?,
    @Json(name = "bearers") val bearersETag: String?,
    @Json(name = "streams") val streamsEtag: String?,
    @Json(name = "logo") val logoEtag: String?,
    @Json(name = "logo_small") val logoSmallETag: String?
) {

    constructor(eTags: ETags) : this(
        eTags.metadataETag,
        eTags.bearersETag,
        eTags.streamsETag,
        eTags.logoETag,
        eTags.logoSmallETag
    )
}