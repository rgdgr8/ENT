package de.esolutions.connectedradioclient.handlers.media

import de.esolutions.connectedradioclient.data.CompleteStatus
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.ProviderIdentifier
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.StationMetadata
import de.esolutions.connectedradioclient.data.WebStream
import de.esolutions.connectedradioclient.data.backend.lookup.result.DataObject
import de.esolutions.connectedradioclient.data.backend.lookup.result.ETagResult
import de.esolutions.connectedradioclient.data.bearer.IBearer
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.logger.LogInfo
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags

object SendToMedia {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    /**
     * Create [ProviderIdentifier], [StationMetadata], [WebStream]-list and [IBearer]-list from the
     * given [mcc], backend[data] and sends all information to the media [callback].
     * Send no [CompleteStatus].
     */
    fun metadata(
        logInfo: LogInfo?,
        mcc: String?,
        eTags: ETagResult?,
        data: DataObject,
        callback: IRadioStationDataCallback
    ) {
        val providerIdentifiers = ProviderIdentifier.createListFromBackendData(data.providers)
        val stationMetadata = StationMetadata.createFromBackendData(providerIdentifiers, data)
        val webStreams = data.streams?.let {
            WebStream.createListFromBackendData(it)
        }
        val bearers = data.bearers?.let {
            IBearer.createListFromBackendData(mcc ?: "", it)
        }

        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send metadata to media: $providerIdentifiers, $stationMetadata, $bearers, $webStreams",
            LoggerTags.MEDIA,
            LoggerTags.LOOKUP
        )
        callback.onMetadata(eTags?.metadataETag, stationMetadata)
        webStreams?.let {
            callback.onStreams(eTags?.streamsEtag, it)
        }
        bearers?.let {
            callback.onBearers(eTags?.bearersETag, it)
        }
    }

    /**
     * Send the given [stationLogo] to the media [callback].
     * Send [CompleteStatus] too.
     */
    fun logo(
        logInfo: LogInfo?,
        etag: String?,
        stationLogo: StationLogo,
        callback: IRadioStationDataCallback,
        contentExpiry: Long?
    ) {
        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send logo to media: $stationLogo",
            LoggerTags.MEDIA,
            LoggerTags.LOGO
        )
        callback.onLogo(etag, stationLogo)
        completeStatus(logInfo, callback, contentExpiry)
    }

    /**
     * Send delete for the normal and the small [StationLogo] to the given [callback].
     * Send [CompleteStatus] too.
     */
    fun deleteLogo(logInfo: LogInfo?, callback: IRadioStationDataCallback, contentExpiry: Long?) {
        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send no logos to media",
            LoggerTags.MEDIA,
            LoggerTags.LOGO
        )
        callback.onLogo(null, null)
        callback.onSLogo(null, null)
        completeStatus(logInfo, callback, contentExpiry)
    }

    /**
     * Iterate over the given [map] and send no content messages for every callback.
     */
    fun noContentForAll(map: Map<String, MediaRequestObject>, requestId: String?, contentExpiry: Long?) {
        map.forEach { (bearer, value) ->
            val logInfo = LogInfo(bearer, requestId)

            noContent(logInfo, value.callback, contentExpiry)
        }
    }

    /**
     * Send null for all data to indicate we have no content.
     */
    fun noContent(logInfo: LogInfo?, callback: IRadioStationDataCallback, contentExpiry: Long?) {
        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send no content to media",
            LoggerTags.MEDIA,
            LoggerTags.LOGO,
            LoggerTags.LOOKUP
        )
        callback.onMetadata(null, null)
        callback.onLogo(null, null)
        callback.onSLogo(null, null)
        callback.onBearers(null, null)
        callback.onStreams(null, null)
        completeStatus(logInfo, callback, contentExpiry)
    }

    /**
     * Iterate over the given [map] and send error messages for every callback.
     */
    fun errorForAll(retryBehavior: RetryBehavior, map: Map<String, MediaRequestObject>, requestId: String?) {
        map.forEach { (bearer, value) ->
            val logInfo = LogInfo(bearer, requestId)

            error(logInfo, retryBehavior, value.callback)
        }
    }

    /**
     * Send an error message for the given [callback].
     */
    fun error(logInfo: LogInfo?, retryBehavior: RetryBehavior, callback: IRadioStationDataCallback) {
        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send error to media: $retryBehavior",
            LoggerTags.MEDIA,
            LoggerTags.LOGO,
            LoggerTags.LOOKUP
        )
        completeStatus(logInfo, callback, null, retryBehavior, Config.requestConfig.retryDelay)
    }

    /**
     * Calls [retryAfter] for all callbacks within the map.
     *
     * This function iterates over the given [map] and calls [retryAfter] for each callback.
     * It constructs a [LogInfo] object for each callback using the corresponding bearer and [requestId].
     *
     * @param map A map containing bearer strings as keys and [MediaRequestObject] instances as values.
     * @param requestId A unique identifier for the request.
     * @param retryAfter The delay in seconds after which the request should be retried.
     * @param contentExpiry The expiration time in seconds.
     */
    fun retryAfterForAll(
        map: Map<String, MediaRequestObject>,
        requestId: String?,
        retryAfter: Long,
        contentExpiry: Long?
    ) {
        map.forEach { (bearer, value) ->
            val logInfo = LogInfo(bearer, requestId)

            retryAfter(
                logInfo,
                value.callback,
                retryAfter,
                contentExpiry
            )
        }
    }

    /**
     * Sends a retryAfter message to the media callback with the specified delay and content expiry.
     *
     * This function logs an informational message indicating the retryAfter delay and behavior,
     * and then calls the [completeStatus] function with the appropriate parameters.
     *
     * @param logInfo Log information for the request, including bearer and request ID.
     * @param callback Callback to send the retryAfter message to.
     * @param retryAfter Delay in seconds after which the request should be retried.
     * @param contentExpiry Expiration time in seconds.
     */
    fun retryAfter(
        logInfo: LogInfo?,
        callback: IRadioStationDataCallback,
        retryAfter: Long,
        contentExpiry: Long?
    ) {
        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send retryAfter $retryAfter seconds " +
                "to media with behavior ${RetryBehavior.DELAYED}",
            LoggerTags.MEDIA,
            LoggerTags.LOOKUP
        )
        completeStatus(logInfo, callback, contentExpiry, RetryBehavior.DELAYED, retryAfter)
    }

    /**
     * Calls [retryAfterOrBackendPreferences] for all callbacks within the map.
     */
    fun retryAfterOrBackendPreferencesForAll(
        map: Map<String, MediaRequestObject>,
        requestId: String?,
        retryAfter: Long?,
        retryBehavior: RetryBehavior,
        contentExpiry: Long?
    ) {
        map.forEach { (bearer, value) ->
            val logInfo = LogInfo(bearer, requestId)

            retryAfterOrBackendPreferences(
                logInfo,
                value.callback,
                retryAfter,
                retryBehavior,
                contentExpiry
            )
        }
    }

    /**
     * If [retryAfter] is set, and onComplete is sent with it and [RetryBehavior.DELAYED]
     * Else, an onComplete with Config.requestConfig.retryDelay and the provided [retryBehavior] is sent.
     *
     * The [contentExpiry] parameter is always sent in the onComplete
     *
     * Note: [retryAfter] refers to the retryAfter header of the respective response.
     */
    fun retryAfterOrBackendPreferences(
        logInfo: LogInfo?,
        callback: IRadioStationDataCallback,
        retryAfter: Long?,
        retryBehavior: RetryBehavior,
        contentExpiry: Long?
    ) {
        if (retryAfter != null) {
            retryAfter(logInfo, callback, retryAfter, contentExpiry)
        } else {
            Logger.i(
                PrivacyFlags.UNDECIDED,
                logInfo,
                "Send retry ${Config.requestConfig.retryDelay} seconds " +
                    "to media with behavior $retryBehavior",
                LoggerTags.MEDIA,
                LoggerTags.LOOKUP
            )
            completeStatus(logInfo, callback, contentExpiry, retryBehavior, Config.requestConfig.retryDelay)
        }
    }

    /**
     * Iterate over the given [map] and send success messages for every callback.
     */
    fun notModifiedForAll(map: Map<String, MediaRequestObject>, requestId: String?, contentExpiry: Long?) {
        map.forEach { (bearer, value) ->
            val logInfo = LogInfo(bearer, requestId)

            notModified(logInfo, value.callback, contentExpiry)
        }
    }

    /**
     * Sends a [CompleteStatus] to the given [callback] with no retry and an expiration date.
     */
    fun notModified(logInfo: LogInfo?, callback: IRadioStationDataCallback, contentExpiry: Long?) {
        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send not modified to media",
            LoggerTags.MEDIA,
            LoggerTags.LOGO,
            LoggerTags.LOOKUP
        )
        completeStatus(logInfo, callback, contentExpiry)
    }

    /**
     * Send the [CompleteStatus] to the given [callback] with the given
     * [retryBehavior], [retry] and expirationDate if a [contentExpiry] is available.
     *
     * @param logInfo Log information for the request, including bearer and request ID.
     * @param callback Callback to send the onComplete to
     * @param contentExpiry Expiration time in seconds.
     * @param retryBehavior Retry behavior of the MDS
     * @param retry Delay of the MDS retry
     */
    private fun completeStatus(
        logInfo: LogInfo?,
        callback: IRadioStationDataCallback,
        contentExpiry: Long?,
        retryBehavior: RetryBehavior = RetryBehavior.NONE,
        retry: Long? = null,
    ) {
        val completeStatus = CompleteStatus(retryBehavior, retry)

        val expirationDate = if (contentExpiry != null) {
            timeProvider.getCurrentTimeSeconds() + contentExpiry
        } else {
            null
        }

        Logger.i(
            PrivacyFlags.UNDECIDED,
            logInfo,
            "Send completeStatus to media: $completeStatus with expirationDate: $expirationDate " +
                "(contentExpiry=$contentExpiry)",
            LoggerTags.MEDIA,
            LoggerTags.LOGO,
            LoggerTags.LOOKUP
        )
        callback.onComplete(completeStatus, expirationDate)
    }
}