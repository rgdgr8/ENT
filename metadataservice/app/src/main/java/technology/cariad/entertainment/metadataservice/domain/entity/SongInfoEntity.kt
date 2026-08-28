package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for a radio station containing all known information about the station.
 *
 * @param artist name of the songs artist, if available.
 * @param title name of the song, if available.
 * @param album name of the songs album, if available.
 * @param year year the [album] was released, if available.
 * @param coverartUri the uri that can be used to obtain the albums binary coverart data,
 *                    if available.
 * @param era era the song belongs to, if available.
 * @param genre name of the genre that best fits the song, if available.
 * @param mood textual description of the mood, if available.
 * @param tempo beats per minute of the song, if available.
 * @param externalIds identifier of the song from external content providers like spotify,
 *                    amazon music, etc.
 * @param externalAlbumIds identifier of the Album from external content providers like spotify,
 *                    amazon music, etc.
 * @param gracenoteId song's Gracenote identifier, if available
 * @param gracenoteUniqueId song's unique Gracenote identifier, if available
 */
data class SongInfoEntity(
    val artist: String? = null,
    val title: String? = null,
    val album: String? = null,
    val year: String? = null,
    var coverartUri: String? = null,
    val era: String? = null,
    val genre: String? = null,
    val mood: String? = null,
    val tempo: String? = null,
    val externalIds: List<ExternalIdentifierEntity> = listOf(),
    val externalAlbumIds: List<ExternalIdentifierEntity> = listOf(),
    val gracenoteId: String? = null,
    val gracenoteUniqueId: String? = null
)
