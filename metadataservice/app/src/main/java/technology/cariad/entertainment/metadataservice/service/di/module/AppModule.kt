package technology.cariad.entertainment.metadataservice.service.di.module

import android.content.Context
import android.net.ConnectivityManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.common.codingprovider.MediaCodingProvider
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.MediaCodingValues
import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasonsApiClient
import technology.cariad.entertainment.metadataservice.common.disablereasons.DisableReasonsClient
import technology.cariad.entertainment.metadataservice.common.network.INetworkManager
import technology.cariad.entertainment.metadataservice.common.network.NetworkManager
import technology.cariad.entertainment.metadataservice.common.network.StubNetworkManager
import android.car.Car
import android.car.hardware.property.CarPropertyManager
import technology.cariad.entertainment.metadataservice.common.permission.IPermissionManager
import technology.cariad.entertainment.metadataservice.common.permission.PermissionManager
import technology.cariad.entertainment.metadataservice.common.position.PositionProviderApi
import technology.cariad.entertainment.metadataservice.common.serviceconnection.ServiceConnection
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListApiClient
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListClient
import technology.cariad.entertainment.metadataservice.common.serviceresolver.ServiceResolver
import technology.cariad.entertainment.metadataservice.data.provider.ConfigurationProvider
import technology.cariad.entertainment.metadataservice.data.provider.OnlineStateProvider
import technology.cariad.entertainment.metadataservice.data.provider.TimeProvider
import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.IMutableConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.IMutableOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.service.provider.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import javax.inject.Named
import javax.inject.Singleton

@Module(includes = [AppModule.ProviderBindingsModule::class, AppReleaseModule::class])
class AppModule(
    private val appContext: Context,
    private val storagePath: String
) {

    /**
     * Provides the [Context] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideAppContext(): Context {
        return appContext
    }

    /**
     * Provides the single instance of file storage root path to the dependency graph.
     */
    @Provides
    @Named("FileStorageRoot")
    @Singleton
    fun provideFileStorageRoot(): String {
        return storagePath
    }

    /**
     * Provides the [ITimeProvider] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideTimeProvider(): ITimeProvider {
        return TimeProvider()
    }

    /**
     * Provides the [ServiceListClient] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideServiceListClient(
        context: Context,
        dispatcherProvider: DispatcherProvider,
        configurationProvider: IConfigurationProvider
    ): ServiceListClient {

        return ServiceListClient(
            ServiceListApiClient(context),
            configurationProvider,
            dispatcherProvider.io()
        )
    }

    /**
     * Provides the [DisableReasonsClient] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideDisableReasonsClient(
        context: Context,
        dispatcherProvider: DispatcherProvider,
        configurationProvider: IConfigurationProvider
    ): DisableReasonsClient {

        return DisableReasonsClient(
            DisableReasonsApiClient(context),
            dispatcherProvider.io(),
            configurationProvider
        )
    }

    /**
     * Provides the [DisableReasonsClient] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideNetworkAccessProvider(
        context: Context,
        dispatcherProvider: DispatcherProvider
    ): technology.cariad.entertainment.metadataservice.common.networkaccess.INetworkAccessProvider {
        val car = Car.createCar(context)
        val carPropertyService = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

        return technology.cariad.entertainment.metadataservice.common.networkaccess.NetworkAccessProvider(
            technology.cariad.entertainment.metadataservice.common.networkaccess.NetworkAccessProviderApi(
                carPropertyService
            ),
            GlobalScope,
            dispatcherProvider.io()
        )
    }

    /**
     * Provides the [OnlineStateProvider] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideOnlineStateProviderImpl(
        serviceListClient: ServiceListClient,
        disableReasonsClient: DisableReasonsClient,
        networkManager: INetworkManager,
    ): OnlineStateProvider {
        return OnlineStateProvider(
            serviceListClient,
            disableReasonsClient,
            networkManager,
            MediaCodingValues.countryOfSales?.countryCode ?: "--"
        )
    }

    /**
     * Provides the [ConfigurationProvider] single instance to the dependency graph.
     */
    @Provides
    @Singleton
    fun provideConfigurationProviderImpl(): ConfigurationProvider {
        return ConfigurationProvider()
    }

    /**
     * Defines bindings from object instances to specific interfaces. If only a single instance of
     * the consumed type exists, it can be injected in multiple places expecting either of the
     * interfaces.
     */
    @Module
    interface ProviderBindingsModule {

        /**
         * Provides the single [OnlineStateProvider] instance as [IOnlineStateProvider] interface
         * to the dependency graph.
         */
        @Binds
        @Suppress("unused")
        fun provideOnlineStateProvider(
            onlineStateProvider: OnlineStateProvider
        ): IOnlineStateProvider

        /**
         * Provides the single [OnlineStateProvider] instance as [IMutableOnlineStateProvider]
         * interface to the dependency graph.
         */
        @Binds
        @Suppress("unused")
        fun provideMutableOnlineStateProvider(
            onlineStateProvider: OnlineStateProvider
        ): IMutableOnlineStateProvider

        /**
         * Provides the single [ConfigurationProvider] instance as [IConfigurationProvider]
         * interface to the dependency graph.
         */
        @Binds
        @Suppress("unused")
        fun provideConfigurationProvider(
            configurationProvider: ConfigurationProvider
        ): IConfigurationProvider

        /**
         * Provides the single[ConfigurationProvider] instance of [IMutableConfigurationProvider]
         * interface to the dependency graph.
         */
        @Binds
        @Suppress("unused")
        fun provideMutableConfigurationProvider(
            configurationProvider: ConfigurationProvider
        ): IMutableConfigurationProvider
    }

    /**
     * Provides the [INetworkManager] single instance to the dependency graph.
     * For local/dev builds, uses StubNetworkManager to simulate network connectivity.
     */
    @Singleton
    @Provides
    fun provideNetworkManager(context: Context): INetworkManager {
        // Use stub for dev builds to simulate network without real ConnectivityManager
        return if (BuildConfig.FLAVOR.contains("dev")) {
            StubNetworkManager(context)
        } else {
            NetworkManager(
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            )
        }
    }

    /**
     * Provides the [IPositionProvider] single instance to the dependency graph.
     */
    @Singleton
    @Provides
    internal fun providePositionProviderAPI(context: Context): PositionProviderApi {
        val intentAction = "com.volkswagenag.nav.service.PositionInformation.BIND"
        val serviceResolver = ServiceResolver(context)
        val serviceConnection = ServiceConnection(serviceResolver, context, intentAction)
        return PositionProviderApi(serviceConnection, 1000)
    }

    /**
     * Provides the [IPermissionManager] single instance to the dependency graph.
     */
    @Singleton
    @Provides
    fun providePermissionManager(
        context: Context
    ): IPermissionManager {
        return PermissionManager(context)
    }

    /**
     * Provides the [MediaCodingProvider] single instance to the dependency graph.
     */
    @Singleton
    @Provides
    fun provideCodingProvider(context: Context): MediaCodingProvider {
        return MediaCodingProvider(context)
    }
}

