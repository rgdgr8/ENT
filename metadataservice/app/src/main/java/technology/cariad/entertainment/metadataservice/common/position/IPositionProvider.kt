package technology.cariad.entertainment.metadataservice.common.position

import technology.cariad.entertainment.metadataservice.common.util.Result
import kotlinx.coroutines.flow.StateFlow

interface IPositionProvider {

    /**
     * Provides continuous updates of the current geo location.
     * Provides null if no valid position is available.
     * @return flow of [PositionInformation]
     */
    fun positionFlow(): StateFlow<Result<PositionInformation?>>

    /**
     * Returns the last position information.
     */
    suspend fun lastPosition(): Result<PositionInformation?>
}

