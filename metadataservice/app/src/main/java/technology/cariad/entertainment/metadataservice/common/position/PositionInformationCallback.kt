package technology.cariad.entertainment.metadataservice.common.position

import com.volkswagenag.nav.positioninformation.GeneralPositionInformationType
import com.volkswagenag.nav.positioninformation.IPositionInformationCallback
import com.volkswagenag.nav.positioninformation.RoadInformationType
import technology.cariad.entertainment.metadataservice.common.log.PrivacyFlag
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.NO_POSITIONING_DATA_AVAILABLE_ERROR
import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlinx.coroutines.channels.ProducerScope

internal class PositionInformationCallback
private constructor(channel: ProducerScope<GeneralPositionInformationType?>) :
    IPositionInformationCallback.Stub(), ProducerScope<GeneralPositionInformationType?> by channel {

    private val log by logger()

    companion object {
        fun create(channel: ProducerScope<GeneralPositionInformationType?>)
                : PositionInformationCallback {
            return PositionInformationCallback(channel)
        }
    }

    override fun onGeneralPositionInformation(
        generalPositionInformation: GeneralPositionInformationType?
    ) {
        log.info(this::onGeneralPositionInformation.name) {
            "received position update"
        }

        generalPositionInformation?.let { pos ->
            log.info(this::onGeneralPositionInformation.name, PrivacyFlag.GNSS) {
                "lat=${pos.latitude} long=${pos.longitude}"
            }
        }

        trySend(generalPositionInformation)
    }

    override fun onRoadInformation(
        isOnRoad: Boolean,
        roadInformation: RoadInformationType?
    ) {
        log.warn(this::onRoadInformation.name) {
            "not implemented"
        }
    }

    override fun onError(error: Int) {
        log.error(this::onError.name) {
            "code=$error"
        }
        IRCLogger.appError(NO_POSITIONING_DATA_AVAILABLE_ERROR)
        trySend(null)
    }
}

