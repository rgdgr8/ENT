package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasons
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.data.provider.OnlineState
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class LookupMode {
    NONE,
    OFFLINE,
    FULL
}

/**
 * Interface for a provider managing the current online lookup state.
 */
interface IOnlineStateProvider {

    /**
     * Checks if a service with the given id is allowed to request data online.
     * @param serviceId system wide used service identifier
     * @return currently allowed [LookupMode]
     */
    fun isOnlineLookupAllowed(serviceId: ServiceIdentifier): LookupMode

    /**
     * Returns a flow of the service list configuration for the service with the given id.
     * @param serviceId
     * @return state flow of [ServiceListEntry]
     */
    fun serviceListFlow(serviceId: ServiceIdentifier): StateFlow<ServiceListEntry?>

    /**
     * Returns a flow of the disabled reasons for the service with the given id.
     * @param serviceId
     * @return state flow of [DisableReasons]
     */
    fun disableReasonsFlow(serviceId: ServiceIdentifier): StateFlow<DisableReasons?>

    /**
     * Returns a flow of state changes to the network the given service should use.
     * @param serviceId
     * @return state flow of [NetworkInfo]
     */
    fun networkFlow(serviceId: ServiceIdentifier): StateFlow<NetworkInfo?>

    /**
     * Returns a flow of state changes to both the service list and network configuration.
     * The flow emits either a pair of both NetworkInfo and ServiceListEntry, or null.
     * @param serviceId
     * @param onlineOnly if true, requires both network and service list to be present; if false, requires only service list
     * @return flow of Pair<NetworkInfo, ServiceListEntry>>?
     */
    fun onlineStateFlow(
        serviceId: ServiceIdentifier, onlineOnly: Boolean = false
    ): Flow<OnlineState?>
}

