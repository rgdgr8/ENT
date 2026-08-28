package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.FlowResult
import technology.cariad.entertainment.metadataservice.common.util.FlowValue
import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.StationPart
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

private const val MILLISECONDS_PER_SECOND = 1000L
private const val SECONDS_PER_HOUR = 3600

class RadioStationRequest(
    private val dataProvider: IRadioStationDataProvider,
    private val dataStore: IRadioStationDataStore,
    private val timeProvider: ITimeProvider
) {
    private val log by logger()

    private lateinit var cachedStation: StationEntity

    /**
     * Requests the station data for the given bearer.
     * @param bearer Station bearer
     * @param eTags e-tags from a previous response for the same bearer
     * @param onlineLookup Online or offline lookup
     * @return a flow of StationEntity as they become available
     */
    fun stationRequest(
        bearer: IBearerEntity,
        etags: ETagsEntity,
        onlineLookup: Boolean
    ): Flow<FlowResult<StationEntity>> {
        return flow {

            val station = loadCachedStation(bearer)

            val eTagsEntity = ETagsEntity(
                bearerListETag = station?.bearerList?.etag ?: "",
                metadataETag = station?.metadata?.etag ?: "",
                streamsETag = station?.streamList?.etag ?: "",
                logoETag = station?.logo?.eTag ?: "",
                logoSmallETag = station?.logoSmall?.eTag ?: ""
            )

            if (!this@RadioStationRequest::cachedStation.isInitialized) {
                if (station != null) {
                    if (etags != eTagsEntity) {
                        emit(FlowValue(station))
                        log.info(this@RadioStationRequest::stationRequest.name) {
                            "update data - etag changed: old=$etags new=$eTagsEntity"
                        }
                    } else {
                        log.warn(this@RadioStationRequest::stationRequest.name) {
                            "don't update data - etag matches: etag=$etags etag=$eTagsEntity"
                        }
                    }
                    cachedStation = station
                } else {
                    cachedStation = createStation(bearer)
                    log.info(this@RadioStationRequest::stationRequest.name) {
                        "no cached station data available for bearer=${bearer.toBearerString()}"
                    }
                }
            }

            if (onlineLookup) {
                emitAll(onlineStationFlow(bearer, eTagsEntity))
            }
        }
    }

    private fun onlineStationFlow(
        bearer: IBearerEntity,
        eTags: ETagsEntity
    ): Flow<FlowResult<StationEntity>> = flow {
        val lifeCycleData = cachedStation.lifeCycleData!!

        val now = timeProvider.secondsSinceEpoch()
        if (!RequestLogic.updateRequired(lifeCycleData, now)) {

            log.info(this@RadioStationRequest::onlineStationFlow.name) {
                "no update required for bearer=${bearer.toBearerString()}"
            }

            return@flow
        }

        if (!RequestLogic.updatedAllowed(lifeCycleData, now)) {
            lifeCycleData.retryAfter?.let {

                val waitSeconds = (it - now)

                //TODO: refine maximum time for active retry
                if (waitSeconds <= SECONDS_PER_HOUR) {
                    log.info(this@RadioStationRequest::onlineStationFlow.name) {
                        "next retry for bearer=${bearer.toBearerString()} after " +
                                "$waitSeconds seconds, waiting..."
                    }

                    delay(waitSeconds * MILLISECONDS_PER_SECOND)
                } else {
                    log.info(this@RadioStationRequest::onlineStationFlow.name) {
                        "next retry for bearer=${bearer.toBearerString()} after " +
                                "$waitSeconds seconds, check on next request"
                    }
                    return@flow
                }
            } ?: return@flow
        }

        do {
            val status = requestStation(bearer, eTags)

            if (status == null) {
                log.error(this@RadioStationRequest::onlineStationFlow.name) {
                    "invalid online data received for bearer=${bearer.toBearerString()}"
                }
                return@flow
            }

            log.info(this@RadioStationRequest::onlineStationFlow.name) {
                "online data received for bearer=${bearer.toBearerString()}"
            }

            //TODO: only emit if changed?
            emit(FlowValue(cachedStation))

            val waitSeconds = status.lifeCycleData?.retryAfter?.let {
                it - timeProvider.secondsSinceEpoch()
            }
            waitSeconds?.let {

                log.info(this@RadioStationRequest::onlineStationFlow.name) {
                    "online data incomplete for bearer=${bearer.toBearerString()}, " +
                            "retry after $waitSeconds seconds..."
                }

                delay(waitSeconds * MILLISECONDS_PER_SECOND)
            }

        } while (status?.lifeCycleData?.retryAfter != null)
    }


    private suspend fun loadCachedStation(bearer: IBearerEntity): StationEntity? {
        return dataStore.getDataForBearer(bearer)
    }

    private suspend fun createStation(bearer: IBearerEntity): StationEntity {
        val stationEntity = StationEntity(
            lifeCycleData = LifeCycleDataEntity(lastRequested = timeProvider.secondsSinceEpoch())
        )
        dataStore.updateLifeCycleData(bearer, stationEntity.lifeCycleData!!)
        return stationEntity
    }

    private suspend fun requestStation(
        bearer: IBearerEntity,
        etags: ETagsEntity
    ): StationEntity? {
        val backendUpdates =
            dataProvider.getDataForBearer(bearer, etags)
        return handleUpdate(bearer, backendUpdates)
    }

    private suspend fun handleUpdate(
        bearer: IBearerEntity,
        updates: List<Pair<StationPart, Any?>>
    ): StationEntity? {
        updates.forEach { pair ->
            when (pair.first) {
                StationPart.BEARER -> {
                    //type mismatch
                    if (pair.second !is BearerListEntity) return@forEach
                    val bearerList = pair.second as BearerListEntity

                    bearerList.bearers = bearerList.bearers.toMutableSet()
                        .also { it.add(bearer) }.toList()

                    //no change, we don't care about order
                    if (
                        cachedStation.bearerList?.bearers?.toSet() == bearerList.bearers.toSet()
                    ) return@forEach

                    cachedStation.bearerList = bearerList
                    dataStore.updateBearers(bearer, bearerList)
                }
                StationPart.METADATA -> {
                    //removed
                    if (pair.second == null) {
                        cachedStation.metadata = null
                        dataStore.deleteMetadata(bearer)
                        return@forEach
                    }

                    //type mismatch
                    if (pair.second !is StationMetadataEntity) return@forEach
                    val metadata = pair.second as StationMetadataEntity

                    //no change
                    if (cachedStation.metadata == metadata) return@forEach

                    cachedStation.metadata = metadata
                    dataStore.updateMetadata(bearer, metadata)
                }
                StationPart.LOGO -> {
                    //removed
                    if (pair.second == null) {
                        cachedStation.logo = null
                        dataStore.deleteLogo(bearer, ImageEntity.Companion.ImageType.LOGO)
                        return@forEach
                    }

                    if (pair.second !is ImageEntity) return@forEach
                    val logo = pair.second as ImageEntity

                    //TODO: check if logo changed/delete old logo
                    cachedStation.logo = dataStore.updateLogo(bearer, logo)
                }
                StationPart.LOGO_SMALL -> {
                    //removed
                    if (pair.second == null) {
                        cachedStation.logoSmall = null
                        dataStore.deleteLogo(bearer, ImageEntity.Companion.ImageType.LOGO_SMALL)
                        return@forEach
                    }

                    if (pair.second !is ImageEntity) return@forEach
                    val logo = pair.second as ImageEntity

                    //TODO: check if logo changed/delete old logo
                    cachedStation.logoSmall = dataStore.updateLogo(bearer, logo)
                }
                StationPart.STREAM -> {
                    if (pair.second !is StationStreamListEntity) return@forEach
                    val streamList = pair.second as StationStreamListEntity

                    if (
                        cachedStation.streamList?.streams?.toSet() == streamList.streams.toSet()
                    ) return@forEach

                    cachedStation.streamList = streamList
                    dataStore.updateStreams(bearer, streamList)
                }
                StationPart.PROVIDER_IDENTIFIERS -> {
                    if (pair.second !is List<*>) return@forEach
                    val providerIds =
                        (pair.second as List<*>).filterIsInstance<ProviderIdentifierEntity>()

                    if (
                        cachedStation.providerIdentifiers.toSet() == providerIds.toSet()
                    ) return@forEach

                    cachedStation.providerIdentifiers = providerIds
                    dataStore.updateProviderIdentifiers(bearer, providerIds)
                }
                StationPart.LIFE_CYCLE_DATA -> {
                    if (pair.second !is LifeCycleDataEntity) return@forEach
                    var lifeCycleData = pair.second as LifeCycleDataEntity

                    log.info(this::handleUpdate.name) { "life cycle data update $lifeCycleData" }

                    cachedStation.lifeCycleData?.let {

                        lifeCycleData = RequestLogic.retryDelay(
                            it,
                            lifeCycleData,
                            timeProvider.secondsSinceEpoch()
                        )

                        it.retryDelay = lifeCycleData.retryDelay
                        it.retryAfter = lifeCycleData.retryAfter
                        it.retryStrategy = lifeCycleData.retryStrategy
                        it.retryCount = lifeCycleData.retryCount
                        it.expirationDate = lifeCycleData.expirationDate
                        lifeCycleData.lastRequested = it.lastRequested

                        log.info(this::handleUpdate.name) {
                            "update existing life cycle data: ${cachedStation.lifeCycleData}"
                        }

                        dataStore.updateLifeCycleData(bearer, lifeCycleData)
                    }

                    return cachedStation
                }
            }
        }
        return null
    }
}
