package de.esolutions.connectedradioclient.debug

import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.ProviderIdentifier
import de.esolutions.connectedradioclient.data.bearer.IBearer
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.debug.data.DemoBearer
import de.esolutions.connectedradioclient.debug.data.DemoCompleteStatus
import de.esolutions.connectedradioclient.debug.data.DemoProviderIdentifier
import de.esolutions.connectedradioclient.debug.data.DemoStationLogo
import de.esolutions.connectedradioclient.debug.data.DemoStationMetadata
import de.esolutions.connectedradioclient.debug.data.DemoWebStream
import de.esolutions.connectedradioclient.handlers.backend.controller.IController
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import kotlin.random.Random

class DebugController : IController {

    private lateinit var debugConfig: DebugConfig
    private val demoProviderIdentifier: DemoProviderIdentifier = DemoProviderIdentifier()
    private val demoStationMetadata: DemoStationMetadata = DemoStationMetadata()
    private val demoBearer: DemoBearer = DemoBearer()
    private val demoWebStream: DemoWebStream = DemoWebStream()
    private val demoStationLogo: DemoStationLogo = DemoStationLogo()
    private val demoCompleteStatus: DemoCompleteStatus = DemoCompleteStatus()
    private val timeProvider: TimeProvider = DefaultTimeProvider()

    /**
     * Set the debug config.
     */
    fun setDebugConfig(debugConfig: DebugConfig) {
        this.debugConfig = debugConfig
    }

    override fun getRadioStationData(
        mediaRequestObject: MediaRequestObject
    ) {
        val callback = mediaRequestObject.callback
        val bearer = mediaRequestObject.bearer
        val providerIdentifier = this.demoProviderIdentifier.getDemoObject()

        if (this.calculateProbability(this.debugConfig.sendError)) {
            val completeStatus = this.demoCompleteStatus.getErrorStatus()
            callback.onComplete(completeStatus, null)
            return
        }

        this.metadata(providerIdentifier, callback, bearer)
        this.bearers(callback, bearer)
        this.streams(callback)
        this.slogo(bearer, callback)
        this.logo(bearer, callback)

        val completeStatus = this.demoCompleteStatus.getSuccessStatus()
        callback.onComplete(
            completeStatus,
            timeProvider.getCurrentTimeSeconds() + this.debugConfig.expireInSeconds
        )
    }

    private fun calculateProbability(acceptanceValue: Double): Boolean {
        val randomNumber = Random.nextDouble()
        return randomNumber < acceptanceValue
    }

    private fun waitForDelay() {
        val delay = Random.nextInt(this.debugConfig.minDelay, this.debugConfig.maxDelay) * 1000L
        Thread.sleep(delay)
    }

    private fun metadata(
        providerIdentifier: ProviderIdentifier,
        callback: IRadioStationDataCallback,
        bearer: IBearer
    ) {
        if (this.debugConfig.deleteMetadata) {
            callback.onMetadata(null, null)
        } else if (this.calculateProbability(this.debugConfig.sendMetadata)) {
            this.waitForDelay()
            val stationMetadata =
                this.demoStationMetadata.getDemoObject(
                    listOf(providerIdentifier),
                    bearer.getBearerString()
                )
            callback.onMetadata("metadata_etag", stationMetadata)
        }
    }

    private fun bearers(
        callback: IRadioStationDataCallback,
        bearer: IBearer
    ) {
        if (this.debugConfig.deleteBearerList) {
            callback.onBearers(null, null)
        } else if (this.calculateProbability(this.debugConfig.sendBearerList)) {
            this.waitForDelay()
            val bearers = this.demoBearer.getDemoList(bearer)
            callback.onBearers("bearer_etag", bearers)
        }
    }

    private fun streams(
        callback: IRadioStationDataCallback
    ) {
        if (this.debugConfig.deleteWebStreams) {
            callback.onStreams(null, null)
        } else if (this.calculateProbability(this.debugConfig.sendWebStreams)) {
            this.waitForDelay()
            val webStreams = this.demoWebStream.getDemoList()
            callback.onStreams("stream_etag", webStreams)
        }
    }

    private fun slogo(
        bearer: IBearer,
        callback: IRadioStationDataCallback
    ) {
        if (this.debugConfig.deleteSLogo) {
            callback.onSLogo(null, null)
        } else if (this.calculateProbability(this.debugConfig.sendSLogo)) {
            this.waitForDelay()
            val pngStationLogo = this.demoStationLogo.getDemoObject(bearer, "PNG")
            callback.onSLogo("logo_small_etag", pngStationLogo)
        }
    }

    private fun logo(
        bearer: IBearer,
        callback: IRadioStationDataCallback
    ) {
        if (this.debugConfig.deleteLogo) {
            callback.onLogo(null, null)
        } else if (this.calculateProbability(this.debugConfig.sendLogo)) {
            this.waitForDelay()
            val webPStationLogo = this.demoStationLogo.getDemoObject(bearer, "WEBP")
            callback.onLogo("logo_etag", webPStationLogo)
        }
    }
}