package technology.cariad.entertainment.metadataservice.domain

/**
 * Interface through which the configuration can be modified.
 */
interface IMutableConfigurationProvider : IConfigurationProvider {

    /**
     * Set the operation mode.
     * @param mode Either [IConfigurationProvider.OPERATION_MODE_LIVE] or
     * [IConfigurationProvider.OPERATION_MODE_DEMO]
     */
    fun setOperationMode(mode: String)

    /**
     * Enables or disables the evaluation of the service list.
     * If the check is disabled, all operations are allowed.
     * @param enable
     */
    fun setServiceListCheckEnabled(enable: Boolean)

    /**
     * Enables or disables the evaluation of the disable reasons.
     * If the check is disabled, no disable reasons are set.
     * @param enable
     */
    fun setDisableReasonsCheckEnabled(enable: Boolean)

    /**
     * Overrides the back end used for connected radio requests.
     * If the url is empty, the one retrieved from service list is used.
     * @param url
     */
    fun setConradBackendUrlOverride(url: String)

    /**
     * Overrides the position for radio station requests.
     * If the position is empty, the one retrieved from navigation is used.
     * @param position
     */
    fun setPositionOverride(position: String)
}
