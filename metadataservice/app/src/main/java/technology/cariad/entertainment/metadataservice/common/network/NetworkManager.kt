package technology.cariad.entertainment.metadataservice.common.network

import android.net.ConnectivityManager
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

class NetworkManager(
    private val connectivityManager: ConnectivityManager,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : INetworkManager {

    private val log by logger()

    private val networkInfoFlows = mutableMapOf<Pair<Apn, Boolean>, SharedFlow<NetworkInfo?>>()

    private var scope = CoroutineScope(dispatcher)

    /**
     * @see [INetworkManager.getNetworkInfoFlow]
     */
    @Synchronized
    override fun getNetworkInfoFlow(apn: Apn, tethering: Boolean): SharedFlow<NetworkInfo?> {
        log.info(this::getNetworkInfoFlow.name) {
            "retrieve network for apn=$apn: tethering=$tethering"
        }

        return networkInfoFlows.getOrPut(apn to tethering) { buildNetworkInfoFlow(apn, tethering) }
    }

    private fun buildNetworkInfoFlow(apn: Apn, tethering: Boolean): SharedFlow<NetworkInfo?> {
        return requestNetwork(apn, tethering)
            .conflate()
            .distinctUntilChanged()
            .onEach {
                log.info("networkInfo_onEach") {
                    "network changed for apn=$apn tethering=$tethering: network=${it?.network}"
                }
            }
            .onCompletion {
                log.info("networkInfo_onCompletion") {
                    "release network for apn=$apn tethering=$tethering"
                }

                emit(null)
            }
            .shareIn(
                scope,
                started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
                replay = 1
            )
    }

    private fun requestNetwork(apn: Apn, tethering: Boolean): Flow<NetworkInfo?> {
        return callbackFlow {
            val transportTypes = getTransportTypes(apn, tethering)
            val capabilities = getNetworkCapabilities(apn)
            val request = buildNetworkRequest(transportTypes, capabilities)
            val networkCallback = NetworkCallback.create(apn, connectivityManager, this)

            log.info("requestNetwork") {
                "registerDefaultNetworkCallback instead of requesting " +
                        "for specific network due to PANS"
            }

            connectivityManager.registerDefaultNetworkCallback(networkCallback)

            awaitClose {
                log.info("requestNetwork_onClose") {
                    "unregister network callback for apn=$apn tethering=$tethering"
                }

                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }
    }
}

