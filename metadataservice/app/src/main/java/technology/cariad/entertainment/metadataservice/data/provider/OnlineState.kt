package technology.cariad.entertainment.metadataservice.data.provider

import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasons
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry

data class OnlineState(
    val serviceListEntry: ServiceListEntry?,
    val disableReasons: DisableReasons?,
    val networkInfo: NetworkInfo?,
    val onlineLookupEnabled: Boolean,
    val countryOfSales: String
)

