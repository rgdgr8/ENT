package technology.cariad.entertainment.metadataservice.common.position

import technology.cariad.entertainment.metadataservice.common.log.PrivacyFlag.GNSS
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.Error
import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.common.util.Success
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

/**
 * PositionProvider that allows to set a fake position by emitting a value from [fakePosition].
 * A fake position takes precedence over a real position, if it is set.
 * @Note: Use for development purpose only.
 */
class FakePositionProvider(
    private val positionProvider: IPositionProvider,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher
) : IPositionProvider {
    private val log by logger()

    /**
     * Emit Success(null) or Error() for default behavior, emit fake position as
     * Success(PositionInformation).
     */
    val fakePosition = MutableStateFlow<Result<PositionInformation?>>(Success(null))
    private val stateFlow = subscribePosition()

    /**
     * @see IPositionProvider.positionFlow
     */
    override fun positionFlow(): StateFlow<Result<PositionInformation?>> {
        return stateFlow
    }

    /**
     * @see IPositionProvider.lastPosition
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
        return combine(positionProvider.positionFlow(), fakePosition) { real, fake ->

            log.info(this::subscribePosition.name, GNSS) {
                "real=$real, fake=$fake"
            }

            if ((fake as? Success)?.result != null) fake else real
        }.stateIn(
            scope + dispatcher,
            SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
            Success(null)
        )
    }
}

