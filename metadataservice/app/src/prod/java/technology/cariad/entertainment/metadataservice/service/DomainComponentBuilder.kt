package technology.cariad.entertainment.metadataservice.service

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
     * Builder of the Domain Component Builder
     */
    fun build(app: AppComponent, config: DemoConfigProvider): Map<String, DomainComponent> {
        return mapOf(
            "live" to DaggerDomainComponent.builder()
                .appComponent(app)
                .dataModule(DataModule(MODULE_CONFIG))
                .domainModule(DomainModule())
                .providerModule(ProviderModule(MODULE_CONFIG))
                .providerRegionModule(ProviderRegionModule())
                .repositoryModule(RepositoryModule())
                .build()
        )
    }
}

