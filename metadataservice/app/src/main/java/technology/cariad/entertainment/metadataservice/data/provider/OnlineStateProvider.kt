package technology.cariad.entertainment.metadataservice.data.provider

import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasons
import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasonsClient
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.network.INetworkManager
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.IServiceListClient
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.domain.IMutableOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.LookupMode
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn

class OnlineStateProvider(
    private val serviceListClient: IServiceListClient,
    private val disableReasonsClient: DisableReasonsClient,
    private val networkManager: INetworkManager,
    private val countryOfSales: String
) : IMutableOnlineStateProvider {

    private val log by logger()

    @Volatile
    private var onlineLookupEnabled = true

    private var scope = CoroutineScope(Dispatchers.IO)

    private val serviceListFlows = mutableMapOf<ServiceIdentifier, StateFlow<ServiceListEntry?>>()
    private val disableReasonsFlows = mutableMapOf<ServiceIdentifier, StateFlow<DisableReasons?>>()
    private val networkFlows = mutableMapOf<ServiceIdentifier, StateFlow<NetworkInfo?>>()

    /**
     * @see [IMutableOnlineStateProvider.updateOnlineLookupState]
     */
    @Synchronized
    override fun updateOnlineLookupState(allowed: Boolean): Boolean {
        onlineLookupEnabled = allowed
        return onlineLookupEnabled
    }

    /**
     * @see [IMutableOnlineStateProvider.isOnlineLookupAllowed]
     */
    override fun isOnlineLookupAllowed(serviceId: ServiceIdentifier): LookupMode {
        if (!serviceListClient.serviceEnabled(serviceId)) return LookupMode.NONE
        if (!onlineLookupEnabled || disableReasonsClient.hasDisableReasons(serviceId)) {
            return LookupMode.OFFLINE
        }
        return LookupMode.FULL
    }

    /**
     * @see [IMutableOnlineStateProvider.serviceListFlow]
     */
    @Synchronized
    override fun serviceListFlow(serviceId: ServiceIdentifier): StateFlow<ServiceListEntry?> {
        return serviceListFlows.getOrPut(serviceId) {
            serviceListClient.serviceListFlow()
                .onSubscription {
                    log.info(this@OnlineStateProvider::serviceListFlows.name) {
                        "started collection for serviceId=$serviceId"
                    }
                }
                .map { serviceList ->
                    serviceList.firstOrNull { it.serviceId == serviceId }
                }
                .onCompletion {
                    log.info(this@OnlineStateProvider::serviceListFlows.name) {
                        "completed collection for serviceId=$serviceId"
                    }
                }
                .stateIn(
                    scope,
                    SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
                    null
                )
        }
    }

    /**
     * @see [IMutableOnlineStateProvider.disableReasonsFlow]
     */
    @Synchronized
    override fun disableReasonsFlow(serviceId: ServiceIdentifier): StateFlow<DisableReasons?> {
        return disableReasonsFlows.getOrPut(serviceId) {
            disableReasonsClient.disableReasonsFlow()
                .map { disableReasons ->
                    disableReasons.firstOrNull { it.serviceId == serviceId }
                }
                .stateIn(
                    scope,
                    SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
                    null
                )
        }
    }

    /**
     * @see [IMutableOnlineStateProvider.networkFlow]
     */
    @Synchronized
    override fun networkFlow(serviceId: ServiceIdentifier): StateFlow<NetworkInfo?> {
        return networkFlows.getOrPut(serviceId) {
            serviceListFlow(serviceId)
                .flatMapLatest { entry ->
                    entry?.let {
                        networkManager.getNetworkInfoFlow(it.apn, it.isTetheringAllowed)
                    } ?: flowOf(null)
                }
                .stateIn(
                    scope,
                    SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
                    null
                )
        }
    }

    override fun onlineStateFlow(
        serviceId: ServiceIdentifier, onlineOnly: Boolean
    ): Flow<OnlineState?> =
        combine(
            networkFlow(serviceId),
            serviceListFlow(serviceId),
            disableReasonsFlow(serviceId)
        ) { network, serviceList, disableReasons ->
            log.info(this::onlineStateFlow.name) {
                "configuration changed: " +
                        "network=$network, " +
                        "serviceList=$serviceList, " +
                        "disableReasons=$disableReasons" +
                        "countryOfSales=$countryOfSales"
            }
            OnlineState(serviceList, disableReasons, network, onlineLookupEnabled, countryOfSales)
        }.map { onlineState ->
            with(onlineState) {
                if ((onlineOnly && networkInfo != null || !onlineOnly)
                    && serviceListEntry != null &&
                    disableReasons?.hasDisableReasons != true
                ) {
                    this
                } else {
                    null
                }
            }
        }.distinctUntilChanged()
}

