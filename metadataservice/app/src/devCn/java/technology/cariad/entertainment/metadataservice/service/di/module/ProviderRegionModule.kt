package technology.cariad.entertainment.metadataservice.service.di.module

import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.StationPart
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.external.ConRadClient
import technology.cariad.entertainment.metadataservice.service.di.ServiceScope

@Module
class ProviderRegionModule {

    /**
     * Provides single instance of the [ConRadClient] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideConnectedRadioClient(): ConRadClient {
        return ConRadClient()
    }

    /**
     * Provides single instance of the [IRadioStationDataProvider] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRadioStationDataProvider(): IRadioStationDataProvider {
        return object : IRadioStationDataProvider {

            override val serviceId: ServiceIdentifier
                get() = throw NotImplementedError("Functionality is not available in China!")

            override suspend fun getDataForBearer(
                bearer: IBearerEntity,
                eTags: ETagsEntity?
            ): List<Pair<StationPart, Any?>> {
                throw NotImplementedError("Functionality is not available in China!")
            }
        }
    }
}

