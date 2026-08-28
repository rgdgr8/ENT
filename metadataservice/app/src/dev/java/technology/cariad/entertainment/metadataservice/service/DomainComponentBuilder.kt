package technology.cariad.entertainment.metadataservice.service

import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import technology.cariad.entertainment.metadataservice.service.config.DEMO_MODULE_CONFIG
import technology.cariad.entertainment.metadataservice.service.config.DemoConfigProvider
import technology.cariad.entertainment.metadataservice.service.config.MODULE_CONFIG
import technology.cariad.entertainment.metadataservice.service.di.AppComponent
import technology.cariad.entertainment.metadataservice.service.di.DaggerDomainComponent
import technology.cariad.entertainment.metadataservice.service.di.DomainComponent
import technology.cariad.entertainment.metadataservice.service.di.module.DataModule
import technology.cariad.entertainment.metadataservice.service.di.module.DomainModule
import technology.cariad.entertainment.metadataservice.service.di.module.ProviderModule
import technology.cariad.entertainment.metadataservice.service.di.module.ProviderRegionModule
import technology.cariad.entertainment.metadataservice.service.di.module.RepositoryModule

object DomainComponentBuilder {
    /**
     * Build function for the Domain component
     * @param app AppComponent of MetadataService
     * @param config the default shared preferences
     */
    fun build(app: AppComponent, config: DemoConfigProvider): Map<String, DomainComponent> {

        val liveDomain = DaggerDomainComponent.builder()
            .appComponent(app)
            .dataModule(DataModule(MODULE_CONFIG))
            .domainModule(DomainModule())
            .providerModule(ProviderModule(MODULE_CONFIG))
            .providerRegionModule(ProviderRegionModule())
            .repositoryModule(RepositoryModule())
            .build()

        val demoDomain = DaggerDomainComponent.builder()
            .appComponent(app)
            .dataModule(DataModule(DEMO_MODULE_CONFIG))
            .domainModule(DomainModule())
            .providerModule(ProviderModule(DEMO_MODULE_CONFIG))
            .providerRegionModule(ProviderRegionModule())
            .repositoryModule(RepositoryModule())
            .build()

        demoDomain.conRadClient().setDebugConfig(config.demoConfig.toDebugConfig())

        if(config.conradTokenOverrideEnabled) {
            liveDomain.conRadClient().setToken(config.conradToken)
        }

        return mapOf(
            IConfigurationProvider.OPERATION_MODE_LIVE to liveDomain,
            IConfigurationProvider.OPERATION_MODE_DEMO to demoDomain
        )
    }
}

