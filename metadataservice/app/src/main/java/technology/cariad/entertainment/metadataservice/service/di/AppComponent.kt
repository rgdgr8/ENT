package technology.cariad.entertainment.metadataservice.service.di

import android.content.Context
import dagger.Component
import de.esolutions.gracenoteservice.gntest.FrameworkConfigUnpacker
import technology.cariad.entertainment.metadataservice.common.codingprovider.MediaCodingProvider
import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasonsClient
import technology.cariad.entertainment.metadataservice.common.network.INetworkManager
import technology.cariad.entertainment.metadataservice.common.networkaccess.INetworkAccessProvider
import technology.cariad.entertainment.metadataservice.common.permission.IPermissionManager
import technology.cariad.entertainment.metadataservice.common.position.IPositionProvider
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListClient
import technology.cariad.entertainment.metadataservice.common.truststore.TrustStoreClient
import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.IMutableConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.IMutableOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.service.MetadataServiceImpl
import technology.cariad.entertainment.metadataservice.service.di.module.AppModule
import technology.cariad.entertainment.metadataservice.service.di.module.DispatcherModule
import technology.cariad.entertainment.metadataservice.service.provider.DispatcherProvider
import javax.inject.Named
import javax.inject.Singleton

@Component(modules = [AppModule::class, DispatcherModule::class])
@Singleton
interface AppComponent {

    /**
     * Injects dependencies into the [MetadataServiceImpl].
     */
    fun inject(metadataService: MetadataServiceImpl)

    /**
     * Injects dependencies into the [FrameworkConfigUnpacker].
     */
    fun inject(frameworkConfigUnpacker: FrameworkConfigUnpacker)

    /**
     * Returns the [Context] instance from the dependency graph.
     */
    fun getContext(): Context

    /**
     * Returns the file storage root path.
     */
    @Named("FileStorageRoot")
    fun getFileStorageRoot(): String

    /**
     * Returns the [DispatcherProvider] instance from the dependency graph.
     */
    fun getDispatcherProvider(): DispatcherProvider

    /**
     * Returns the [ITimeProvider] instance from the dependency graph.
     */
    fun getTimeProvider(): ITimeProvider

    /**
     * Returns the [ServiceListClient] instance from the dependency graph.
     */
    fun getServiceListClient(): ServiceListClient

    /**
     * Returns the [DisableReasonsClient] instance from the dependency graph.
     */
    fun getDisableReasonsClient(): DisableReasonsClient

    /**
     * Returns the [TrustStoreClient] instance from the dependency graph.
     */
    fun getTrustStoreClient(): TrustStoreClient

    /**
     * Returns the [IOnlineStateProvider] instance from the dependency graph.
     */
    fun getOnlineStateProvider(): IOnlineStateProvider

    /**
     * Returns the [IMutableOnlineStateProvider] instance from the dependency graph.
     */
    fun getMutableOnlineStateProvider(): IMutableOnlineStateProvider

    /**
     * Returns the [INetworkAccessProvider] instance from the dependency graph.
     */
    fun getNetworkAccessProvider(): INetworkAccessProvider

    /**
     * Returns the [IConfigurationProvider] instance from the dependency graph.
     */
    fun getConfigurationProvider(): IConfigurationProvider

    /**
     * Returns the [IMutableConfigurationProvider] instance from the dependency graph.
     */
    fun getMutableConfigurationProvider(): IMutableConfigurationProvider

    /**
     * Returns the [INetworkManager] instance from the dependency graph.
     */
    fun getNetworkManager(): INetworkManager

    /**
     * Returns the [IPositionProvider] instance from the dependency graph.
     */
    fun getPositionProvider(): IPositionProvider

    /**
     * Returns the [IPermissionManager] instance from the dependency graph.
     */
    fun getPermissionManager(): IPermissionManager

    /**
     * Returns the [MediaCodingProvider] instance from the dependency graph.
     */
    fun getCodingProvider(): MediaCodingProvider
}

