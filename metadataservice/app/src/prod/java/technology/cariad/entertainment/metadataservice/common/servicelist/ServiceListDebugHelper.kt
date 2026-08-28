package technology.cariad.entertainment.metadataservice.common.servicelist

import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider

internal fun applyDebugConfig(
    serviceList: List<ServiceListEntry>,
    @Suppress("UNUSED_PARAMETER") configurationProvider: IConfigurationProvider
): List<ServiceListEntry> {
    return serviceList
}

