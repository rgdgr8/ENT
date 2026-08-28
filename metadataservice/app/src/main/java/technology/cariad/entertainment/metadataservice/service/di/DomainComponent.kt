package technology.cariad.entertainment.metadataservice.service.di

import dagger.Component
import technology.cariad.entertainment.metadataservice.service.di.module.DataModule
import technology.cariad.entertainment.metadataservice.service.di.module.DomainModule
import technology.cariad.entertainment.metadataservice.service.di.module.ProviderModule
import technology.cariad.entertainment.metadataservice.service.di.module.RepositoryModule

@Component(
    modules = [
        RepositoryModule::class,
        ProviderModule::class,
        DataModule::class,
        DomainModule::class
    ],
    dependencies = [AppComponent::class]
)
@ServiceScope
interface DomainComponent : IDomainRegionComponent

