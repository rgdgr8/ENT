package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier

/**
 * An interface for services retrieving data from online sources.
 */
interface IOnlineService {

    /**
     * The system wide used service identifier
     */
    val serviceId: ServiceIdentifier

    /**
     * Update the network this [IOnlineService] should use for all requests going forward.
     * @param networkInfo The new network. May be null if no network is available or online requests
     *                    are not allowed.
     */
    fun selectNetwork(networkInfo: NetworkInfo?) {}

    /**
     * Update the service configuration of this [IOnlineService]
     * @param serviceListEntry The new configuration. May be null if the configuration has not
     *                         been received or the service is disabled.
     */
    fun setServiceConfig(serviceListEntry: ServiceListEntry?) {}
}

