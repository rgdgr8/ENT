package de.esolutions.connectedradioclient.handlers.backend.controller

import android.content.Context
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.handlers.backend.LookupRequestContainer
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import de.esolutions.connectedradioclient.logger.LogInfo
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags

class RequestController(context: Context) : IController {

    private var requestContainers: MutableMap<String?, LookupRequestContainer> = mutableMapOf()
    private val tokenHandler = TokenHandler(context)
    private val handleRegistration = HandleRegistration(tokenHandler)

    /**
     * Add the given [mediaRequestObject] to the container.
     */
    @Synchronized
    override fun getRadioStationData(
        mediaRequestObject: MediaRequestObject
    ) {
        Logger.i(
            PrivacyFlags.SAFE,
            LogInfo(mediaRequestObject.bearer.getBearerString(), null),
            "Received MediaRequestObject: $mediaRequestObject",
            LoggerTags.MEDIA,
            LoggerTags.LOOKUP
        )
        createContainerAndAddMediaRequest(mediaRequestObject)
    }

    /**
     * If the container is null or locked, create a new one.
     * If not, add the given [mediaRequestObject] to the container.
     */
    @Synchronized
    private fun createContainerAndAddMediaRequest(mediaRequestObject: MediaRequestObject) {
        val mcc = mediaRequestObject.mcc
        val requestContainer = requestContainers[mcc]

        requestContainer?.let {
            if (it.cancelDebounceJob()) {
                it.addRequest(mediaRequestObject)
                return
            }
        }

        requestContainers[mcc] = LookupRequestContainer(handleRegistration, tokenHandler, mcc)

        createContainerAndAddMediaRequest(mediaRequestObject)
    }
}
