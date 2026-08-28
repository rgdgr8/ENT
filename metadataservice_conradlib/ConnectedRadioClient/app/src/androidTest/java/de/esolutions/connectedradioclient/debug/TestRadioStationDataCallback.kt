package de.esolutions.connectedradioclient.debug

import de.esolutions.connectedradioclient.data.CompleteStatus
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.StationMetadata
import de.esolutions.connectedradioclient.data.WebStream
import de.esolutions.connectedradioclient.data.bearer.IBearer
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import java.util.concurrent.CountDownLatch

class TestRadioStationDataCallback(
    private val latch: CountDownLatch? = null
) : IRadioStationDataCallback {
    companion object {
        const val METADATA = 0x01 //0000 0001
        const val LOGO = 0x02 //0000 0010
        const val SLOGO = 0x04 //0000 0100
        const val STREAMS = 0x08 //0000 1000
        const val BEARERS = 0x10 //0001 0000
        const val COMPLETE = 0x20 //0010 0000
    }

    var metadata: StationMetadata? = null
    var logo: StationLogo? = null
    var sLogo: StationLogo? = null
    var streamList: List<WebStream>? = null
    var bearerList: List<IBearer>? = null

    var completeStatus: CompleteStatus? = null
    var expirationDate: Long? = null
    var calledFunctions: Int = 0x00

    var metadataETag: String? = null
    var logoETag: String? = null
    var logoSmallETag: String? = null
    var bearerETag: String? = null
    var streamETag: String? = null

    override fun onMetadata(etag: String?, metadata: StationMetadata?) {
        this.metadataETag = etag
        this.metadata = metadata
        calledFunctions = calledFunctions or METADATA
    }

    override fun onLogo(etag: String?, logo: StationLogo?) {
        this.logoETag = etag
        this.logo = logo
        calledFunctions = calledFunctions or LOGO
    }

    override fun onSLogo(etag: String?, logo: StationLogo?) {
        this.logoSmallETag = etag
        this.sLogo = logo
        calledFunctions = calledFunctions or SLOGO
    }

    override fun onStreams(etag: String?, streams: List<WebStream>?) {
        this.streamETag = etag
        this.streamList = streams
        calledFunctions = calledFunctions or STREAMS
    }

    override fun onBearers(etag: String?, bearers: List<IBearer>?) {
        this.bearerETag = etag
        this.bearerList = bearers
        calledFunctions = calledFunctions or BEARERS
    }

    override fun onComplete(status: CompleteStatus, expirationDate: Long?) {
        this.completeStatus = status
        this.expirationDate = expirationDate
        calledFunctions = calledFunctions or COMPLETE
        latch?.let { latch.countDown() }
    }
}