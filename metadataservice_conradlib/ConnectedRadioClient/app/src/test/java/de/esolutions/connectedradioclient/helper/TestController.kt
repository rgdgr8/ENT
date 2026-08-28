package de.esolutions.connectedradioclient.helper

import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.handlers.backend.controller.IController
import java.util.concurrent.CountDownLatch

class TestController(
    private val latch: CountDownLatch? = null
) : IController {
    companion object {
        const val DATA = 0x1 //0001
    }

    var calledFunktions: Int = 0x0
    var mediaRequestObject: MediaRequestObject? = null
    var apn: String? = null

    override fun getRadioStationData(mediaRequestObject: MediaRequestObject) {
        this.mediaRequestObject = mediaRequestObject
        calledFunktions = calledFunktions or DATA
        latch?.countDown()
    }
}