package technology.cariad.entertainment.metadataservice.data.provider

import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ConfigurationProviderTest {

    private lateinit var configurationProvider: ConfigurationProvider

    @Before
    fun setup() {
        configurationProvider = ConfigurationProvider()
    }

    @Test
    fun getOperationMode_shouldReturnDefaultLive() {
        val mode = configurationProvider.getOperationMode()

        Assert.assertEquals(IConfigurationProvider.OPERATION_MODE_LIVE, mode)
    }

    @Test
    fun getOperationMode_shouldReturnLastSetMode() {
        configurationProvider.setOperationMode(IConfigurationProvider.OPERATION_MODE_DEMO)

        val demoMode = configurationProvider.getOperationMode()
        Assert.assertEquals(IConfigurationProvider.OPERATION_MODE_DEMO, demoMode)
    }

    @Test
    fun setOperationMode_shouldAcceptValidModes() {
        configurationProvider.setOperationMode(IConfigurationProvider.OPERATION_MODE_DEMO)

        val demoMode = configurationProvider.getOperationMode()
        Assert.assertEquals(IConfigurationProvider.OPERATION_MODE_DEMO, demoMode)

        configurationProvider.setOperationMode(IConfigurationProvider.OPERATION_MODE_LIVE)

        val liveMode = configurationProvider.getOperationMode()
        Assert.assertEquals(IConfigurationProvider.OPERATION_MODE_LIVE, liveMode)
    }

    @Test(expected = AssertionError::class)
    fun setOperationMode_shouldFailOnEmptyMode() {
        configurationProvider.setOperationMode("")
    }

    @Test(expected = AssertionError::class)
    fun setOperationMode_shouldFailOnInvalidMode() {
        configurationProvider.setOperationMode("test")
    }
}

