package technology.cariad.entertainment.metadataservice.service.di.module

import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.domain.ICoverArtRepository
import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataRepository
import technology.cariad.entertainment.metadataservice.domain.ISongInfoRepository
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsRepository
import technology.cariad.entertainment.metadataservice.service.coverart.TextBasedCoverArtLookupFactory
import technology.cariad.entertainment.metadataservice.service.di.ServiceScope
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveAlbumMetadataFactory
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveArtistMetadataFactory
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveRecordingMetadataByIdFactory
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveRecordingMetadataByNameFactory
import technology.cariad.entertainment.metadataservice.service.radiostationasia.RetrieveRadioStationAsiaFactory
import technology.cariad.entertainment.metadataservice.service.radiostationdata.RetrieveRadioStationDataFactory
import technology.cariad.entertainment.metadataservice.service.songinfo.RetrieveSongInfoFactory
import technology.cariad.entertainment.metadataservice.service.songlyrics.RetrieveSongLyricsFactory

@Module
class DomainModule {

    /**
     * Provides single instance of the [RetrieveRadioStationDataFactory] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveRadioStationDataFactory(
        radioStationDataRepository: IRadioStationDataRepository
    ): RetrieveRadioStationDataFactory {
        return RetrieveRadioStationDataFactory(radioStationDataRepository)
    }

    /**
     * Provides single instance of the [RetrieveRadioStationAsiaFactory] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveRadioStationDataAsiaFactory(
        radioStationDataAsiaRepository: IRadioStationDataAsiaRepository
    ): RetrieveRadioStationAsiaFactory {
        return RetrieveRadioStationAsiaFactory(radioStationDataAsiaRepository)
    }

    /**
     * Provides single instance of the [RetrieveSongInfoFactory] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveSongInfoFactory(
        songInfoRepository: ISongInfoRepository
    ): RetrieveSongInfoFactory {
        return RetrieveSongInfoFactory(songInfoRepository)
    }

    /**
     * Provides single instance of the [RetrieveSongLyricsFactory] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveSongLyricsFactory(
        songInfoRepository: ISongInfoRepository,
        songLyricsRepository: ISongLyricsRepository
    ): RetrieveSongLyricsFactory {
        return RetrieveSongLyricsFactory(songInfoRepository, songLyricsRepository)
    }

    /**
     * Provides single instance of the [TextBasedCoverArtLookupFactory] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveCoverArtDataFactory(
        coverArtRepository: ICoverArtRepository
    ): TextBasedCoverArtLookupFactory {
        return TextBasedCoverArtLookupFactory(coverArtRepository)
    }

    /**
     * Provides single instance of the [RetrieveAlbumMetadataFactory] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveAlbumMetadataFactory(
        musicMetadataRepository: IMusicMetadataRepository
    ): RetrieveAlbumMetadataFactory {
        return RetrieveAlbumMetadataFactory(musicMetadataRepository)
    }

    /**
     * Provides single instance of the [RetrieveArtistMetadataFactory] to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveArtistMetadataFactory(
        musicMetadataRepository: IMusicMetadataRepository
    ): RetrieveArtistMetadataFactory {
        return RetrieveArtistMetadataFactory(musicMetadataRepository)
    }

    /**
     * Provides single instance of the [RetrieveRecordingMetadataByIdFactory]
     * to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveRecordingMetadataByIdFactory(
        musicMetadataRepository: IMusicMetadataRepository
    ): RetrieveRecordingMetadataByIdFactory {
        return RetrieveRecordingMetadataByIdFactory(musicMetadataRepository)
    }

    /**
     * Provides single instance of the [RetrieveRecordingMetadataByNameFactory]
     * to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRetrieveRecordingMetadataByNameFactory(
        musicMetadataRepository: IMusicMetadataRepository
    ): RetrieveRecordingMetadataByNameFactory {
        return RetrieveRecordingMetadataByNameFactory(musicMetadataRepository)
    }
}
