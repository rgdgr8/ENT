package technology.cariad.entertainment.metadataservice.common.servicelist

import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier

internal fun applyDebugConfig(
    serviceList: List<ServiceListEntry>,
    configurationProvider: IConfigurationProvider
): List<ServiceListEntry> {
    return serviceList.map { entry ->
        if (entry.serviceId == ServiceIdentifier.CONNECTEDRADIO_V2
            && configurationProvider.getConradBackendUrlOverride().isNotEmpty()
        ) {
            val url = configurationProvider.getConradBackendUrlOverride()
            val httpOperation = entry.defaultHttpOperation?.copy(url = url)
            entry.copy(defaultHttpOperation = httpOperation)
        } else {
            entry
        }
    }
}
