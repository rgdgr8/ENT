package technology.cariad.entertainment.metadataservice.domain

/**
 * Interface for configurable values.
 */
interface IConfigurationProvider {

    companion object {
        const val OPERATION_MODE_LIVE = "live"
        const val OPERATION_MODE_DEMO = "demo"
    }

    /**
     * Retrieve the current operation mode.
     * @return [OPERATION_MODE_LIVE] or [OPERATION_MODE_DEMO]
     */
    fun getOperationMode(): String

    /**
     * @return true if service list should be evaluated, false otherwise
     */
    fun getServiceListCheckEnabled(): Boolean

    /**
     * @return true if disable reasons should be evaluated, false otherwise
     */
    fun getDisableReasonsCheckEnabled(): Boolean

    /**
     * @return the connected radio back end URL, that should be used instead of the one received
     *         from service list; may be empty to indicate default handling
     */
    fun getConradBackendUrlOverride(): String

    /**
     * @return the override position, that should be used instead of the one received from
     *         navigation; may be empty to indicate default handling
     */
    fun getPositionOverride(): String
}

