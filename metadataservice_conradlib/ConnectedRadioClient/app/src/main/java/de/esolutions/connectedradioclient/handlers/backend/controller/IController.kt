package de.esolutions.connectedradioclient.handlers.backend.controller

import de.esolutions.connectedradioclient.data.MediaRequestObject

/**
 * Only internal interface to handle several controller ([RequestController], [DebugController]).
 */
interface IController {
    /**
     * Receive the [mediaRequestObject] with all information about the requested station.
     */
    fun getRadioStationData(mediaRequestObject: MediaRequestObject)
}