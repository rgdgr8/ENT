package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * Data structure encapsulation the result of the song recognition.
 */
data class SongRecognitionResult(
    val title: String? = null,
    val album: String? = null,
    val artist: String? = null,
    val genre: String? = null,
    val year: String? = null,
    val mood: String? = null,
    val tempo: String? = null,
    val externalIds: List<ExternalId> = listOf(),
    val externalAlbumIds: List<ExternalId> = listOf(),
    val image: ByteArray? = null,
    val gracenoteId: String? = null,
    val gracenoteUniqueId: String? = null
) {
    //generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SongRecognitionResult

        if (title != other.title) return false
        if (album != other.album) return false
        if (artist != other.artist) return false
        if (genre != other.genre) return false
        if (year != other.year) return false
        if (mood != other.mood) return false
        if (tempo != other.tempo) return false
        if (externalIds != other.externalIds) return false
        if (externalAlbumIds != other.externalAlbumIds) return false
        if (image != null) {
            if (other.image == null) return false
            if (!image.contentEquals(other.image)) return false
        } else if (other.image != null) return false
        if (gracenoteId != other.gracenoteId) return false
        if (gracenoteUniqueId != other.gracenoteUniqueId) return false

        return true
    }

    //generated
    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (genre?.hashCode() ?: 0)
        result = 31 * result + (year?.hashCode() ?: 0)
        result = 31 * result + (mood?.hashCode() ?: 0)
        result = 31 * result + (tempo?.hashCode() ?: 0)
        result = 31 * result + externalIds.hashCode()
        result = 31 * result + externalAlbumIds.hashCode()
        result = 31 * result + (image?.contentHashCode() ?: 0)
        result = 31 * result + (gracenoteId?.hashCode() ?: 0)
        result = 31 * result + (gracenoteUniqueId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "SongRecognitionResult(" +
                "title=$title, " +
                "album=$album, " +
                "artist=$artist, " +
                "genre=$genre, " +
                "year=$year, " +
                "mood=$mood, " +
                "tempo=$tempo, " +
                "externalIds=$externalIds, " +
                "externalAlbumIds=$externalAlbumIds, " +
                "image size=${image?.size}B, " +
                "gracenoteId=$gracenoteId, " +
                "gracenoteUniqueId=$gracenoteUniqueId)"
    }
}

