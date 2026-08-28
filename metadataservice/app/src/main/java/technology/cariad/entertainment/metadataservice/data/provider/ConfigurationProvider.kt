package technology.cariad.entertainment.metadataservice.data.provider

import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.IMutableConfigurationProvider

/**
 * Implementation of the ConfigurationProvider.
 */
class ConfigurationProvider : IMutableConfigurationProvider {

    private var operationMode = IConfigurationProvider.OPERATION_MODE_LIVE
    private var serviceListCheckEnabled = true
    private var disableReasonsCheckEnabled = true
    private var conradBackendUrlOverride = ""
    private var positionOverride = ""

    /**
     * @see [IMutableConfigurationProvider.setOperationMode]
     */
    override fun setOperationMode(mode: String) {
        assert(mode == IConfigurationProvider.OPERATION_MODE_LIVE ||
                mode == IConfigurationProvider.OPERATION_MODE_DEMO)
        operationMode = mode
    }

    /**
     * @see [IMutableConfigurationProvider.setServiceListCheckEnabled]
     */
    override fun setServiceListCheckEnabled(enable: Boolean) {
        serviceListCheckEnabled = enable
    }

    /**
     * @see [IMutableConfigurationProvider.setDisableReasonsCheckEnabled]
     */
    override fun setDisableReasonsCheckEnabled(enable: Boolean) {
        disableReasonsCheckEnabled = enable
    }

    /**
     * @see [IMutableConfigurationProvider.setConradBackendUrlOverride]
     */
    override fun setConradBackendUrlOverride(url: String) {
        conradBackendUrlOverride = url
    }

    /**
     * @see [IMutableConfigurationProvider.setPositionOverride]
     */
    override fun setPositionOverride(position: String) {
        positionOverride = position
    }

    /**
     * @see [IConfigurationProvider.getOperationMode]
     */
    override fun getOperationMode(): String {
        return operationMode
    }

    /**
     * @see [IMutableConfigurationProvider.getServiceListCheckEnabled]
     */
    override fun getServiceListCheckEnabled(): Boolean {
        return serviceListCheckEnabled
    }

    /**
     * @see [IMutableConfigurationProvider.getDisableReasonsCheckEnabled]
     */
    override fun getDisableReasonsCheckEnabled(): Boolean {
        return disableReasonsCheckEnabled
    }

    /**
     * @see [IMutableConfigurationProvider.getConradBackendUrlOverride]
     */
    override fun getConradBackendUrlOverride(): String {
        return conradBackendUrlOverride
    }

    /**
     * @see [IMutableConfigurationProvider.getPositionOverride]
     */
    override fun getPositionOverride(): String {
        return positionOverride
    }
}

