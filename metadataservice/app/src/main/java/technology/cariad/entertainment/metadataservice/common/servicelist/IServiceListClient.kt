package technology.cariad.entertainment.metadataservice.common.servicelist

import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for clients providing access to the service configuration from the service list.
 */
interface IServiceListClient {

    /**
     * @return a [StateFlow] representing the last received service list configuration.
     */
    fun serviceListFlow(): StateFlow<List<ServiceListEntry>>

    /**
     * Checks if the service is enabled.
     * @param serviceId The [ServiceIdentifier] of the service.
     * @return true if the service is enabled, false otherwise
     */
    fun serviceEnabled(serviceId: ServiceIdentifier): Boolean
}

