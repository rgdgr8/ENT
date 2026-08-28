package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.FlowCompleted
import technology.cariad.entertainment.metadataservice.common.util.FlowValue
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataRepository
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformWhile

class RadioStationDataRepository(
    private val dataProvider: IRadioStationDataProvider,
    private val dataStore: IRadioStationDataStore,
    private val timeProvider: ITimeProvider,
    private val onlineStateProvider: IOnlineStateProvider
) : IRadioStationDataRepository {

    private val log by logger()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        onlineStateProvider.onlineStateFlow(dataProvider.serviceId).onEach {

            log.info("init") {
                "update network=${it?.networkInfo} and service list=${it?.serviceListEntry}"
            }

            dataProvider.selectNetwork(it?.networkInfo)
            dataProvider.setServiceConfig(it?.serviceListEntry)
        }.launchIn(scope)
    }

    override fun retrieveRadioStationData(
        bearer: IBearerEntity, etags: ETagsEntity
    ): Flow<StationEntity> {
        return stationFlow(bearer, etags)
    }

    private fun stationFlow(bearer: IBearerEntity, etags: ETagsEntity): Flow<StationEntity> {
        val request = RadioStationRequest(dataProvider, dataStore, timeProvider)

        return onlineStateProvider.onlineStateFlow(dataProvider.serviceId, false)
            .conflate()
            .flatMapLatest { onlineState ->
                if (onlineState == null) {
                    log.info(this@RadioStationDataRepository::retrieveRadioStationData.name) {
                        "service disabled"
                    }

                    emptyFlow()
                } else {
                    val onlineLookup = onlineState.networkInfo != null &&
                            onlineState.onlineLookupEnabled

                    request.stationRequest(bearer, etags, onlineLookup)
                        .onCompletion { emit(FlowCompleted) }
                }
            }
            .transformWhile { value ->
                if (value is FlowValue) {
                    emit(value.value).also {
                        log.info(this@RadioStationDataRepository::retrieveRadioStationData.name) {
                            "update station: bearer=$bearer station=${value.value}"
                        }
                    }
                }
                value !is FlowCompleted
            }
            .onCompletion {
                log.info(this@RadioStationDataRepository::retrieveRadioStationData.name) {
                    "finished request for bearer=$bearer"
                }
            }
    }
}

