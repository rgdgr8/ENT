package technology.cariad.entertainment.metadataservice

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasonsClient
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListClient
import technology.cariad.entertainment.metadataservice.common.truststore.TrustStoreClient
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.service.AppComponentBuilder
import technology.cariad.entertainment.metadataservice.service.DomainComponentBuilder
import technology.cariad.entertainment.metadataservice.service.config.DemoConfigProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class MetadataServiceAppTest {

    init {
        LoggerMock()
    }

    private val contextMock = mockk<Context>(relaxed = true)
    private val intentMock = mockk<Intent>(relaxed = true)

    private val serviceListClientMock = mockk<ServiceListClient>(relaxed = true)
    private val disableReasonsClientMock = mockk<DisableReasonsClient>(relaxed = true)
    private val trustStoreClientMock = mockk<TrustStoreClient>(relaxed = true)

    private val testScope = TestScope()

    @Before
    fun setup() {
        mockkConstructor(Intent::class)

        every {
            anyConstructed<Intent>().setPackage(any())
        } returns (intentMock)
    }

    @Test
    fun daggerShouldCreateComponentGraph() {
        MetadataServiceApp.appComponent = AppComponentBuilder.build(contextMock, "")
        MetadataServiceApp.config =
            DemoConfigProvider(PreferenceManager.getDefaultSharedPreferences(contextMock))

        mockkObject(MetadataServiceApp.appComponent)

        every {
            MetadataServiceApp.appComponent.getServiceListClient()
        } returns serviceListClientMock

        every {
            MetadataServiceApp.appComponent.getDisableReasonsClient()
        } returns disableReasonsClientMock

        every {
            MetadataServiceApp.appComponent.getTrustStoreClient()
        } returns trustStoreClientMock

        AppComponentBuilder.initialize(MetadataServiceApp.appComponent, testScope)
        MetadataServiceApp.domainComponent = DomainComponentBuilder.build(
            MetadataServiceApp.appComponent,
            MetadataServiceApp.config
        )

        Assert.assertNotNull(MetadataServiceApp.appComponent)
        Assert.assertNotNull(MetadataServiceApp.config)
        Assert.assertNotNull(MetadataServiceApp.domainComponent)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

