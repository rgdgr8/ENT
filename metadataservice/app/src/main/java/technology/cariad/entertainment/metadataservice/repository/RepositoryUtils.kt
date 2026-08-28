package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.flow.first

/**
 * Checks if the service list entry for the given service is available.
 * @param onlineStateProvider
 * @param serviceId
 * @return true if the entry is available, false otherwise
 */
fun serviceListEntryAvailable(
    onlineStateProvider: IOnlineStateProvider,
    serviceId: ServiceIdentifier
): Boolean {
    return onlineStateProvider.serviceListFlow(serviceId).value != null
}

/**
 * Suspends the current coroutine until the service list entry for the given service becomes
 * available.
 * @param onlineStateProvider
 * @param serviceId
 */
suspend fun delayUntilServiceListEntryAvailable(
    onlineStateProvider: IOnlineStateProvider,
    serviceId: ServiceIdentifier
) {
    onlineStateProvider.serviceListFlow(serviceId).first {
        it != null
    }
}

/**
 * Checks if the network for the given service is available.
 * @param onlineStateProvider
 * @param serviceId
 * @return true if the network is available, false otherwise
 */
fun networkAvailable(
    onlineStateProvider: IOnlineStateProvider,
    serviceId: ServiceIdentifier
): Boolean {
    return onlineStateProvider.networkFlow(serviceId).value != null
}

/**
 * Suspends the current coroutine until the network for the given service becomes available.
 * @param onlineStateProvider
 * @param serviceId
 */
suspend fun delayUntilNetworkAvailable(
    onlineStateProvider: IOnlineStateProvider,
    serviceId: ServiceIdentifier
) {
    onlineStateProvider.networkFlow(serviceId).first {
        it != null
    }
}

