package technology.cariad.entertainment.metadataservice.common.networkaccess

import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

class NetworkAccessProvider(
    private val networkAccessProviderApi: NetworkAccessProviderApi,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher
) : INetworkAccessProvider {
    private val log by logger()

    private val stateFlow = subscribeMcc()

    /**
     * @see [INetworkAccessProvider.mccFlow]
     */
    override fun mccFlow(): StateFlow<Int?> {
        return stateFlow
    }

    private fun subscribeMcc(): StateFlow<Int?> {
        return networkAccessProviderApi.networkAccessFlow()
            .map { mcc ->
                log.info("networkAccessFlow_map") {
                    "new mcc received: mcc=$mcc"
                }

                mcc
            }.catch {
                log.warn("networkAccessFlow_catch") {
                    "failed to retrieve mcc: message=${it.message}"
                }

                emit(null)
            }.stateIn(
                scope + dispatcher,
                SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
                null
            )
    }
}

