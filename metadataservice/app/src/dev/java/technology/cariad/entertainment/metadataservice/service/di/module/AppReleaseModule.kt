package technology.cariad.entertainment.metadataservice.service.di.module

import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.common.position.FakePositionProvider
import technology.cariad.entertainment.metadataservice.common.position.IPositionProvider
import technology.cariad.entertainment.metadataservice.common.position.PositionProvider
import technology.cariad.entertainment.metadataservice.common.position.PositionProviderApi
import technology.cariad.entertainment.metadataservice.service.provider.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
class AppReleaseModule {

    private val appScope = CoroutineScope(Dispatchers.Default)

    /**
     * Provides the [IPositionProvider] development instance to the dependency graph.
     */
    @Singleton
    @Provides
    fun providePositionProvider(
        positionProviderApi: PositionProviderApi,
        dispatcherProvider: DispatcherProvider
    ): IPositionProvider {
        return FakePositionProvider(
            PositionProvider(positionProviderApi, appScope, dispatcherProvider.io()),
            appScope,
            dispatcherProvider.io()
        )
    }
}

