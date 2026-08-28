package technology.cariad.entertainment.metadataservice.service.di.module

import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.service.provider.DispatcherProvider
import javax.inject.Singleton

@Module
class DispatcherModule {

    /**
     * Provides the [DispatcherProvider] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider {
        return DispatcherProvider()
    }
}

