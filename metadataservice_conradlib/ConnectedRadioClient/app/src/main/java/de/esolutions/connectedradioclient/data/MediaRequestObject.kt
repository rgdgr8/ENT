package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.bearer.DabBearer
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.bearer.IBearer
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback

data class MediaRequestObject(
    val bearer: IBearer,
    val eTags: ETags,
    val callback: IRadioStationDataCallback
) {

    val mcc: String?
        get() {
            if (bearer is FmBearer) {
                return bearer.mcc
            } else if (bearer is DabBearer) {
                return bearer.mcc
            }

            return null
        }
}