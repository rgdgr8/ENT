package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.backend.lookup.result.StreamObject
import de.esolutions.connectedradioclient.data.enums.StreamQuality
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WebStreamTest {

    @Test
    fun parse_correct_from_backend_data() {
        val streamList = listOf(StreamObject("url", 0, "high"))
        val webStreams = WebStream.createListFromBackendData(streamList)
        assertEquals(1, webStreams.size)
        val webStream = webStreams[0]
        assertEquals("url", webStream.url)
        assertEquals(0, webStream.offsetMillis)
        assertEquals(StreamQuality.HIGH, webStream.streamQuality)
    }
}