package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity

interface IMusicMetadataProvider : IOnlineService {

    /**
     * Retrieves album metadata for the given [albumId].
     * @param albumId
     * @return [MusicMetadataEntity]
     */
    suspend fun getAlbumMetadata(albumId: String): MusicMetadataEntity

    /**
     * Retrieves artist metadata for the given [artistId].
     * @param artistId
     * @return [MusicMetadataEntity]
     */
    suspend fun getArtistMetadata(artistId: String): MusicMetadataEntity

    /**
     * Retrieves recording metadata for the given [recordingId].
     * @param recordingId
     * @return [MusicMetadataEntity]
     */
    suspend fun getRecordingMetadata(recordingId: String): MusicMetadataEntity

    /**
     * Retrieves recording metadata for the given [recordingName] and [artistName].
     * @param recordingName
     * @param artistName
     * @return [MusicMetadataEntity]
     */
    suspend fun getRecordingMetadataByName(
        recordingName: String,
        artistName: String
    ): MusicMetadataEntity
}

