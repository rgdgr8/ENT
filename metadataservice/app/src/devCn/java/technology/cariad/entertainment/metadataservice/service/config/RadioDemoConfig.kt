package technology.cariad.entertainment.metadataservice.service.config

import android.os.Parcelable
import de.esolutions.connectedradioclient.handlers.media.DebugConfig
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
    fun toDebugConfig(): DebugConfig {
        return DebugConfig();
    }
}

