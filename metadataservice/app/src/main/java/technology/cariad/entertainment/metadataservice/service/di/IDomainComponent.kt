package technology.cariad.entertainment.metadataservice.service.di

import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.service.coverart.TextBasedCoverArtLookupFactory
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveAlbumMetadataFactory
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveArtistMetadataFactory
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveRecordingMetadataByIdFactory
import technology.cariad.entertainment.metadataservice.service.musicmetadata.RetrieveRecordingMetadataByNameFactory
import technology.cariad.entertainment.metadataservice.service.radiostationasia.RetrieveRadioStationAsiaFactory
import technology.cariad.entertainment.metadataservice.service.radiostationdata.RetrieveRadioStationDataFactory
import technology.cariad.entertainment.metadataservice.service.songinfo.RetrieveSongInfoFactory
import technology.cariad.entertainment.metadataservice.service.songlyrics.RetrieveSongLyricsFactory
import javax.inject.Named

interface IDomainComponent {

    /**
     * Returns the [RetrieveRadioStationDataFactory] instance from the dependency graph.
     */
    fun retrieveRadioStationDataFactory(): RetrieveRadioStationDataFactory

    /**
     * Returns the [RetrieveRadioStationDataFactory] instance from the dependency graph.
     */
    fun retrieveSongInfoFactory(): RetrieveSongInfoFactory

    /**
     * Returns the [RetrieveSongLyricsFactory] instance from the dependency graph.
     */
    fun retrieveSongLyricsFactory(): RetrieveSongLyricsFactory

    /**
     * Returns the [TextBasedCoverArtLookupFactory] instance from the dependency graph.
     */
    fun textBasedCoverArtLookupFactory(): TextBasedCoverArtLookupFactory

    /**
     * Returns the [RetrieveRadioStationAsiaFactory] instance from the dependency graph.
     */
    fun retrieveRadioStationAsiaFactory(): RetrieveRadioStationAsiaFactory

    /**
     * Returns the [RetrieveAlbumMetadataFactory] instance from the dependency graph.
     */
    fun retrieveAlbumMetadataFactory(): RetrieveAlbumMetadataFactory

    /**
     * Returns the [RetrieveArtistMetadataFactory] instance from the dependency graph.
     */
    fun retrieveArtistMetadataFactory(): RetrieveArtistMetadataFactory

    /**
     * Returns the [RetrieveRecordingMetadataByIdFactory] instance from the dependency graph.
     */
    fun retrieveRecordingMetadataByIdFactory(): RetrieveRecordingMetadataByIdFactory

    /**
     * Returns the [RetrieveRecordingMetadataByNameFactory] instance from the dependency graph.
     */
    fun retrieveRecordingMetadataByNameFactory(): RetrieveRecordingMetadataByNameFactory

    /**
     * Returns the [CachingDatabase] instance from the dependency graph.
     */
    fun roomDatabase(): CachingDatabase

    /**
     * Returns the ConnectedRadioImageStore instance of [IImageDataStore] from the dependency graph.
     */
    @Named("ConnectedRadioImageStore")
    fun connectedRadioImageStore(): IImageDataStore

    /**
     * Returns the GraceNoteImageStore instance of [IImageDataStore] from the dependency graph.
     */
    @Named("GraceNoteImageStore")
    fun graceNoteImageStore(): IImageDataStore
}

