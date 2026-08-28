package technology.cariad.entertainment.metadataservice.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
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
import technology.cariad.entertainment.metadataservice.data.database.entity.AlbumEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ArtistEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ETagsEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.GenreEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationGenreRankEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationMetadataEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry

@Database(
    entities = [
        AlbumEntry::class,
        ArtistEntry::class,
        BearerEntry::class,
        CoverArtLookupEntry::class,
        GenreEntry::class,
        ImageEntry::class,
        ProviderIdentifierEntry::class,
        StationEntry::class,
        StationMetadataEntry::class,
        StationGenreRankEntry::class,
        StationImageEntry::class,
        StationProviderIdentifierEntry::class,
        StreamEntry::class,
        LifeCycleDataEntry::class,
        ETagsEntry::class],
    exportSchema = true,
    version = 14
)
abstract class CachingDatabase : RoomDatabase() {
    internal abstract fun getAlbumDao(): AlbumDao
    internal abstract fun getArtistDao(): ArtistDao
    internal abstract fun getBearerDao(): BearerDao
    internal abstract fun getCoverArtLookupDao(): CoverArtLookupDao
    internal abstract fun getGenreDao(): GenreDao
    internal abstract fun getImageDao(): ImageDao
    internal abstract fun getProviderIdentifierDao(): ProviderIdentifierDao
    internal abstract fun getStationImageDao(): StationImageDao
    internal abstract fun getStationDao(): StationDao
    internal abstract fun getStationMetadataDao(): StationMetadataDao
    internal abstract fun getStationGenreRankDao(): StationGenreRankDao
    internal abstract fun getStationProviderIdentifierDao(): StationProviderIdentifierDao
    internal abstract fun getStreamDao(): StreamDao
    internal abstract fun getLifeCycleDataDao(): LifeCycleDataDao
    internal abstract fun getETagsDao(): ETagsDao
}

