package technology.cariad.entertainment.metadataservice.common.position

import technology.cariad.entertainment.metadataservice.common.log.PrivacyFlag
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.Error
import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.common.util.Success
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

class PositionProvider(
    private val positionProviderApi: PositionProviderApi,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher
) : IPositionProvider {
    private val log by logger()

    private val stateFlow = subscribePosition()

    /**
     * @see [IPositionProvider.positionFlow]
     */
    override fun positionFlow(): StateFlow<Result<PositionInformation?>> {
        return stateFlow
    }

    /**
     * @see [IPositionProvider.lastPosition]
     */
    override suspend fun lastPosition(): Result<PositionInformation?> {
        return stateFlow.filter {
            when (it) {
                is Success -> it.result != null
                is Error -> true
            }
        }.first()
    }

    private fun subscribePosition(): StateFlow<Result<PositionInformation?>> {
        return positionProviderApi.positionFlow()
            .map {
                val posInf = it?.let { PositionInformation(it.latitude, it.longitude) }

                log.info("subscribePosition_map", PrivacyFlag.GNSS) {
                    "new position received: pos=$posInf"
                }

                Success(posInf)
            }.catch<Result<PositionInformation?>> {
                log.warn("subscribePosition_catch") {
                    "failed to retrieve position information: message=${it.message}"
                }

                emit(Error(it))
            }.stateIn(
                scope + dispatcher,
                SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
                Success(null)
            )
    }
}

