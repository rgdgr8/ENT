package technology.cariad.entertainment.metadataservice.common.network

import android.net.Network
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn

data class NetworkInfo(
    val apn: Apn,
    val network: Network?,
    val interfaceName: String?
)

