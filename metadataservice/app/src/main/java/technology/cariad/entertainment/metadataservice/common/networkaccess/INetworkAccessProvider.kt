package technology.cariad.entertainment.metadataservice.common.networkaccess

import kotlinx.coroutines.flow.StateFlow

interface INetworkAccessProvider {

    /**
     * Provides continuous updates of the current mobile country code (MCC).
     * Provides null if no valid MCC is available
     * @return flow of [Int]
     */
    fun mccFlow(): StateFlow<Int?>
}

