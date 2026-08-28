package technology.cariad.entertainment.metadataservice.service.config

import android.os.Parcelable
import de.esolutions.connectedradioclient.debug.DebugConfig
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RadioDemoConfig(
    var deleteBearerList: Boolean = false,
    var deleteMetadata: Boolean = false,
    var deleteLogo: Boolean = false,
    var deleteSLogo: Boolean = false,
    var deleteWebStreams: Boolean = false,
    var expireInSeconds: Long = 24 * 60 * 60,
    var minDelay: Int = 0,
    var maxDelay: Int = 1,
    var sendBearerList: Int = 100,
    var sendMetadata: Int = 100,
    var sendLogo: Int = 100,
    var sendSLogo: Int = 100,
    var sendWebStreams: Int = 100,
    var sendError: Int = 0
) : Parcelable {
    /**
     * Create a debugConfig from the RadioDemoConfig
     * @return A parceable DebugConfig
     */
    fun toDebugConfig(): DebugConfig {
        val debugConfig = DebugConfig()
        debugConfig.deleteBearerList = deleteBearerList
        debugConfig.deleteMetadata = deleteMetadata
        debugConfig.deleteLogo = deleteLogo
        debugConfig.deleteSLogo = deleteSLogo
        debugConfig.deleteWebStreams = deleteWebStreams
        debugConfig.expireInSeconds = expireInSeconds
        debugConfig.minDelay = minDelay
        debugConfig.maxDelay = maxDelay
        debugConfig.sendBearerList = sendBearerList/100.0
        debugConfig.sendMetadata = sendMetadata/100.0
        debugConfig.sendLogo = sendLogo/100.0
        debugConfig.sendSLogo = sendSLogo/100.0
        debugConfig.sendWebStreams = sendWebStreams/100.0
        debugConfig.sendError = sendError/100.0
        return debugConfig
    }
}

