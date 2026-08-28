package de.esolutions.connectedradioclient.data.backend.lookup.result

import com.squareup.moshi.Json
import de.esolutions.connectedradioclient.data.NO_CHANGES

@Suppress("LongParameterList") // Data structure depends on backend data
data class DataObject(
    @Json(name = "short_name") val shortName: String?,
    @Json(name = "medium_name") val mediumName: String?,
    @Json(name = "long_name") val longName: String?,
    val description: String?,
    val providers: List<ProviderObject>,
    val bearers: List<String>?,
    val streams: List<StreamObject>?,
    val logo: Int? = NO_CHANGES
)