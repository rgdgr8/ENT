package de.esolutions.connectedradioclient.debug.data

import de.esolutions.connectedradioclient.data.WebStream
import de.esolutions.connectedradioclient.data.enums.StreamQuality

class DemoWebStream {

    /**
     * Returns demo web stream list.
     */
    fun getDemoList(): List<WebStream> {
        val webstream1 = WebStream("http://addrad.io/4WRD9v", 0, StreamQuality.LOW)
        val webstream2 = WebStream("http://addrad.io/4WRDbj", 0, StreamQuality.HIGH)
        return listOf(webstream1, webstream2)
    }
}