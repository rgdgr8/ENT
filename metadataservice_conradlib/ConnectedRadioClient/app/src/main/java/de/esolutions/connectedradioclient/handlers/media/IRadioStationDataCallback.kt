package de.esolutions.connectedradioclient.handlers.media

import de.esolutions.connectedradioclient.data.CompleteStatus
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.StationMetadata
import de.esolutions.connectedradioclient.data.WebStream
import de.esolutions.connectedradioclient.data.bearer.IBearer

/**
 * Receive the requested data back or send null if no data is available.
 * If nothing changes since the last request, the method does not send anything.
 * Sends always onComplete at the end.
 */
interface IRadioStationDataCallback {
    /**
     * Receive [metadata] from the requested station or null if no data is available.
     */
    fun onMetadata(etag: String?, metadata: StationMetadata?)

    /**
     * Receive [logo] from the requested station or null if no data is available.
     */
    fun onLogo(etag: String?, logo: StationLogo?)

    /**
     * Receive [logo] from the requested station or null if no data is available.
     */
    fun onSLogo(etag: String?, logo: StationLogo?)

    /**
     * Receive [streams] from the requested station or null if no data is available.
     */
    fun onStreams(etag: String?, streams: List<WebStream>?)

    /**
     * Receive [bearers] from the requested station or null if no data is available.
     */
    fun onBearers(etag: String?, bearers: List<IBearer>?)

    /**
     * The ConRadClient will always call this function after a bearer request finishes.
     * (regardless of whether this is successful or faulty)
     *
     * @param status Defines the retry behavior of the MDS.
     * @param expirationDate UTC timestamp in seconds.
     * If [expirationDate] is null the bearer will be requested at the next client request.
     * => Only sends [expirationDate] if the request was successfully.
     */
    fun onComplete(status: CompleteStatus, expirationDate: Long?)
}