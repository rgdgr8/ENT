package technology.cariad.entertainment.metadataservice.service.di.module

import android.content.Context
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import dagger.Module
import dagger.Provides
import technology.cariad.entertainment.metadataservice.common.database.android.SQLiteDatabaseFactoryDecorator
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabaseEventHandler
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabaseFactory
import technology.cariad.entertainment.metadataservice.data.database.dao.AlbumDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ArtistDao
import technology.cariad.entertainment.metadataservice.data.database.dao.BearerDao
import technology.cariad.entertainment.metadataservice.data.database.dao.CoverArtLookupDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.GenreDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.LifeCycleDataDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationGenreRankDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationMetadataDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StreamDao
import technology.cariad.entertainment.metadataservice.data.datastore.CoverArtDataStore
import technology.cariad.entertainment.metadataservice.data.datastore.RadioStationDataStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.AlbumStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.ArtistStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.CoverArtStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.LifeCycleStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.BearerStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.GenreStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.ImageStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.LifeCycleDataStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.MetadataStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.ProviderIdentifierStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.StationStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.StreamStore
import technology.cariad.entertainment.metadataservice.data.filesystem.ImageDataStore
import technology.cariad.entertainment.metadataservice.data.filesystem.TemporaryImageDataStore
import technology.cariad.entertainment.metadataservice.domain.ICoverArtDataStore
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.service.config.ModuleConfig
import technology.cariad.entertainment.metadataservice.service.di.ServiceScope
import java.nio.file.Paths
import javax.inject.Named

@Module
class DataModule(private val config: ModuleConfig) {

    /**
     * Provides the [CachingDatabase] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideRoomDatabase(
        context: Context,
        @Named("FileStorageRoot") storageRoot: String
    ): CachingDatabase {

        val databasePath = Paths.get(storageRoot, config.databaseFile);

        return CachingDatabaseFactory.create(
            context,
            SQLiteDatabaseFactoryDecorator(
                FrameworkSQLiteOpenHelperFactory(), CachingDatabaseEventHandler(databasePath)
            ),
            databasePath
        )
    }

    /**
     * Provides the [AlbumDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideAlbumDao(cache: CachingDatabase): AlbumDao {
        return cache.getAlbumDao()
    }

    /**
     * Provides the [ArtistDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideArtistDao(cache: CachingDatabase): ArtistDao {
        return cache.getArtistDao()
    }

    /**
     * Provides the [BearerDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideBearerDao(cache: CachingDatabase): BearerDao {
        return cache.getBearerDao()
    }

    /**
     * Provides the [CoverArtLookupDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideCoverArtLookupDao(cache: CachingDatabase): CoverArtLookupDao {
        return cache.getCoverArtLookupDao()
    }

    /**
     * Provides the [ETagsDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideETagsDao(cache: CachingDatabase): ETagsDao {
        return cache.getETagsDao()
    }

    /**
     * Provides the [GenreDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideGenreDao(cache: CachingDatabase): GenreDao {
        return cache.getGenreDao()
    }

    /**
     * Provides the [ImageDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideImageDao(cache: CachingDatabase): ImageDao {
        return cache.getImageDao()
    }

    /**
     * Provides the [LifeCycleDataDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideLifeCycleDataDao(cache: CachingDatabase): LifeCycleDataDao {
        return cache.getLifeCycleDataDao()
    }

    /**
     * Provides the [ProviderIdentifierDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideProviderIdentifierDao(cache: CachingDatabase): ProviderIdentifierDao {
        return cache.getProviderIdentifierDao()
    }

    /**
     * Provides the [StationDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStationDao(cache: CachingDatabase): StationDao {
        return cache.getStationDao()
    }

    /**
     * Provides the [StationGenreRankDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStationGenreRankDao(cache: CachingDatabase): StationGenreRankDao {
        return cache.getStationGenreRankDao()
    }

    /**
     * Provides the [StationImageDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStationImageDao(cache: CachingDatabase): StationImageDao {
        return cache.getStationImageDao()
    }

    /**
     * Provides the [StationMetadataDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStationMetadataDao(cache: CachingDatabase): StationMetadataDao {
        return cache.getStationMetadataDao()
    }

    /**
     * Provides the [StationProviderIdentifierDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStationProviderIdentifierDao(cache: CachingDatabase): StationProviderIdentifierDao {
        return cache.getStationProviderIdentifierDao()
    }

    /**
     * Provides the [StationDao] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStreamDao(cache: CachingDatabase): StreamDao {
        return cache.getStreamDao()
    }

    /**
     * Provides the [BearerStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideBearerStore(bearerDao: BearerDao, eTagsDao: ETagsDao): BearerStore {
        return BearerStore(bearerDao, eTagsDao)
    }

    /**
     * Provides the [GenreStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideGenreStore(
        genreDao: GenreDao,
        stationGenreRankDao: StationGenreRankDao
    ): GenreStore {
        return GenreStore(genreDao, stationGenreRankDao)
    }

    /**
     * Provides the [MetadataStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideMetadataStore(
        stationMetadataDao: StationMetadataDao, eTagsDao: ETagsDao
    ): MetadataStore {
        return MetadataStore(stationMetadataDao, eTagsDao)
    }

    /**
     * Provides the [StreamStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStreamStore(streamDao: StreamDao, eTagsDao: ETagsDao): StreamStore {
        return StreamStore(streamDao, eTagsDao)
    }

    /**
     * Provides the [ImageStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideImageStore(
        stationImageDao: StationImageDao,
        imageDao: ImageDao,
        eTagsDao: ETagsDao
    ): ImageStore {
        return ImageStore(stationImageDao, imageDao, eTagsDao)
    }

    /**
     * Provides the [ProviderIdentifierStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideProviderIdentifierStore(
        providerIdentifierDao: ProviderIdentifierDao,
        stationProviderIdentifierDao: StationProviderIdentifierDao
    ): ProviderIdentifierStore {
        return ProviderIdentifierStore(providerIdentifierDao, stationProviderIdentifierDao)
    }

    /**
     * Provides the [IRadioStationDataStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideLifeCycleDataStore(
        lifeCycleDataDao: LifeCycleDataDao,
        stationDao: StationDao
    ): LifeCycleDataStore {
        return LifeCycleDataStore(lifeCycleDataDao, stationDao)
    }

    /**
     * Provides the connected radio [IImageDataStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    @Named("ConnectedRadioImageStore")
    fun provideConnectedRadioDataStore(
        @Named("FileStorageRoot") storageRoot: String
    ): IImageDataStore {
        return ImageDataStore("${Paths.get(storageRoot, config.conradImageCacheFolder)}")
    }

    /**
     * Provides the GraceNote [IImageDataStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    @Named("GraceNoteImageStore")
    fun provideGraceNoteImageStore(
        context: Context,
        @Named("FileStorageRoot") storageRoot: String
    ): IImageDataStore {
        return ImageDataStore("${Paths.get(storageRoot, config.gracenoteImageCacheFolder)}")
    }

    /**
     * Provides the GraceNoteTemp [IImageDataStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    @Named("GraceNoteTempImageStore")
    fun provideGraceNoteTempImageStore(
        @Named("FileStorageRoot") storageRoot: String
    ): IImageDataStore {
        return TemporaryImageDataStore(
            "${Paths.get(storageRoot, config.gracenoteTempImageCacheFolder)}"
        )
    }

    /**
     * Provides the [AlbumStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideAlbumStore(albumDao: AlbumDao): AlbumStore {
        return AlbumStore(albumDao)
    }

    /**
     * Provides the [ArtistStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideArtistStore(artistDao: ArtistDao): ArtistStore {
        return ArtistStore(artistDao)
    }

    /**
     * Provides the [CoverArtStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideCoverArtStore(coverArtLookupDao: CoverArtLookupDao): CoverArtStore {
        return CoverArtStore(coverArtLookupDao)
    }

    /**
     * Provides the [technology.cariad.entertainment.metadataservice.data.datastore.coverart.ImageStore]
     * single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideCoverArtImageStore(
        imageDao: ImageDao
    ): technology.cariad.entertainment.metadataservice.data.datastore.coverart.ImageStore {
        return technology.cariad.entertainment.metadataservice.data.datastore.coverart.ImageStore(imageDao)
    }

    /**
     * Provides the [LifeCycleStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideLifeCycleStore(
        lifeCycleDataDao: LifeCycleDataDao
    ): LifeCycleStore {
        return LifeCycleStore(lifeCycleDataDao)
    }

    /**
     * Provides the [StationStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideStationStore(
        stationDao: StationDao,
        bearerDao: BearerDao,
        stationImageDao: StationImageDao,
        imageDao: ImageDao,
        @Named("ConnectedRadioImageStore")
        imageDataStore: IImageDataStore,
        eTagsDao: ETagsDao
    ): StationStore {
        return StationStore(
            stationDao,
            bearerDao,
            stationImageDao,
            imageDao,
            imageDataStore,
            eTagsDao
        )
    }

    /**
     * Provides the [CoverArtDataStore] single instance to the dependency graph.
     */
    @ServiceScope
    @Provides
    fun provideCoverArtDataStore(
        cache: CachingDatabase,
        coverArtStore: CoverArtStore,
        albumStore: AlbumStore,
        artistStore: ArtistStore,
        imageStore: technology.cariad.entertainment.metadataservice.data.datastore.coverart.ImageStore,
        @Named("GraceNoteImageStore")
        imageDataStore: IImageDataStore,
        lifeCycleStore: LifeCycleStore,

        ): ICoverArtDataStore {
        return CoverArtDataStore(
            cache,
            coverArtStore,
            albumStore,
            artistStore,
            imageStore,
            imageDataStore,
            lifeCycleStore
        )
    }

    /**
     * Provides the [RadioStationDataStore] single instance to the dependency graph.
     */
    @Suppress("LongParameterList")
    @ServiceScope
    @Provides
    fun provideRadioStationDataStore(
        cache: CachingDatabase,
        bearerStore: BearerStore,
        stationStore: StationStore,
        metadataStore: MetadataStore,
        genreStore: GenreStore,
        imageStore: ImageStore,
        @Named("ConnectedRadioImageStore")
        imageDataStore: IImageDataStore,
        streamStore: StreamStore,
        providerIdentifierStore: ProviderIdentifierStore,
        lifeCycleDataStore: LifeCycleDataStore,
        timeProvider: ITimeProvider
    ): IRadioStationDataStore {
        return RadioStationDataStore(
            cache,
            bearerStore,
            stationStore,
            metadataStore,
            genreStore,
            imageStore,
            imageDataStore,
            streamStore,
            providerIdentifierStore,
            lifeCycleDataStore,
            timeProvider
        )
    }
}

