package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.lookup.result.StreamObject
import de.esolutions.connectedradioclient.data.enums.StreamQuality
import java.util.Locale

data class WebStream(
    val url: String,
    val offsetMillis: Int?,
    val streamQuality: StreamQuality
) {
    //static representation for kotlin
    companion object {
        /**
         * Create a list of [WebStream] objects from the given backend [streamList]
         */
        fun createListFromBackendData(streamList: List<StreamObject>): List<WebStream> {
            return streamList.map {
                WebStream(
                    it.url,
                    it.offset,
                    StreamQuality.valueOf(it.quality.uppercase(Locale.ENGLISH))
                )
            }
        }
    }
}