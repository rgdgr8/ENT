package technology.cariad.entertainment.metadataservice.service.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.common.log.Logger
import technology.cariad.entertainment.metadataservice.data.provider.RadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.external.ConRadClient
import technology.cariad.entertainment.metadataservice.external.MagicAdapter
import technology.cariad.entertainment.metadataservice.service.di.ServiceScope

@Module
class ProviderRegionModule {

    /**
     * Provides single instance of the [ConRadClient] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideConnectedRadioClient(context: Context): ConRadClient {
        return ConRadClient(context, Logger.PROCESS_NAME)
    }

    /**
     * Provides single instance of the [IRadioStationDataProvider] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRadioStationDataProvider(conRadClient: ConRadClient): IRadioStationDataProvider {
        return RadioStationDataProvider(ServiceIdentifier.CONNECTEDRADIO_V2, MagicAdapter().platform, conRadClient)
    }
}

