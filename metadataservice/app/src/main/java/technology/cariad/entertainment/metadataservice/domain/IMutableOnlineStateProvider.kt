package technology.cariad.entertainment.metadataservice.domain

/**
 * Interface through which the online lookup state of the provider can be modified.
 * Usually access should be done via [IOnlineStateProvider]
 */
interface IMutableOnlineStateProvider : IOnlineStateProvider {

    /**
     * Attempt to change the online lookup state to [allowed].
     * This operation may not always succeed due to other conditions checked internally.
     * @param allowed Desired state
     * @return new online lookup state
     */
    fun updateOnlineLookupState(allowed: Boolean): Boolean
}
