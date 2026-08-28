package technology.cariad.entertainment.metadataservice.common.network

import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import kotlinx.coroutines.flow.SharedFlow

interface INetworkManager {

    /**
     * Requests a network for the given [Apn]. Changes to the state of this network are propagated
     * via the returned flow.
     * @param apn name of the access point
     * @param tethering true if tethering over local networks is allowed, false otherwise
     */
    fun getNetworkInfoFlow(apn: Apn, tethering: Boolean): SharedFlow<NetworkInfo?>
}

