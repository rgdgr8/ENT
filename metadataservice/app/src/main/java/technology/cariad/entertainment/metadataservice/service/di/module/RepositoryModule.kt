package technology.cariad.entertainment.metadataservice.service.di.module

import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.common.networkaccess.INetworkAccessProvider
import technology.cariad.entertainment.metadataservice.common.position.IPositionProvider
import technology.cariad.entertainment.metadataservice.domain.ICoverArtDataStore
import technology.cariad.entertainment.metadataservice.domain.ICoverArtProvider
import technology.cariad.entertainment.metadataservice.domain.ICoverArtRepository
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataProvider
import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataRepository
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ISongInfoProvider
import technology.cariad.entertainment.metadataservice.domain.ISongInfoRepository
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsProvider
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsRepository
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.repository.RadioStationDataRepository
import technology.cariad.entertainment.metadataservice.repository.CoverArtRepository
import technology.cariad.entertainment.metadataservice.repository.MusicMetadataRepository
import technology.cariad.entertainment.metadataservice.repository.RadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.repository.SongInfoRepository
import technology.cariad.entertainment.metadataservice.repository.SongLyricsRepository
import technology.cariad.entertainment.metadataservice.service.di.ServiceScope
import javax.inject.Named

@Module
class RepositoryModule {

    /**
     * Provides single instance of the [IRadioStationDataRepository] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun providesRadioStationDataRepository(
        dataProvider: IRadioStationDataProvider,
        dataStore: IRadioStationDataStore,
        timeProvider: ITimeProvider,
        onlineStateProvider: IOnlineStateProvider
    ): IRadioStationDataRepository {
        return RadioStationDataRepository(
            dataProvider,
            dataStore,
            timeProvider,
            onlineStateProvider
        )
    }

    /**
     * Provides single instance of the [IRadioStationDataAsiaRepository] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun providesRadioStationDataAsiaRepository(
        dataProvider: IRadioStationDataAsiaProvider,
        @Named("GraceNoteTempImageStore")
        imageStore: IImageDataStore,
        onlineStateProvider: IOnlineStateProvider,
        positionProvider: IPositionProvider
    ): IRadioStationDataAsiaRepository {
        return RadioStationDataAsiaRepository(
            dataProvider,
            imageStore,
            onlineStateProvider,
            positionProvider
        )
    }

    /**
     * Provides single instance of the [ISongInfoRepository] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun providesSongInfoRepository(
        dataProvider: ISongInfoProvider,
        onlineStateProvider: IOnlineStateProvider,
        @Named("GraceNoteTempImageStore")
        imageStore: IImageDataStore,
    ): ISongInfoRepository {
        return SongInfoRepository(dataProvider, imageStore, onlineStateProvider)
    }

    /**
     * Provides single instance of the [ISongInfoRepository] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun providesSongLyricsRepository(
        dataProvider: ISongLyricsProvider,
        onlineStateProvider: IOnlineStateProvider,
        networkAccessProvider: INetworkAccessProvider
    ): ISongLyricsRepository {
        return SongLyricsRepository(dataProvider, onlineStateProvider, networkAccessProvider)
    }

    /**
     * Provides single instance of the [ICoverArtRepository] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun providesCoverArtDataRepository(
        dataProvider: ICoverArtProvider,
        dataStore: ICoverArtDataStore,
        onlineStateProvider: IOnlineStateProvider,
        timeProvider: ITimeProvider
    ): ICoverArtRepository {
        return CoverArtRepository(
            dataProvider,
            dataStore,
            onlineStateProvider,
            timeProvider
        )
    }

    /**
     * Provides single instance of the [IMusicMetadataRepository] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun providesMusicMetadataRepository(
        dataProvider: IMusicMetadataProvider,
        onlineStateProvider: IOnlineStateProvider
    ): IMusicMetadataRepository {
        return MusicMetadataRepository(
            dataProvider,
            onlineStateProvider
        )
    }
}

