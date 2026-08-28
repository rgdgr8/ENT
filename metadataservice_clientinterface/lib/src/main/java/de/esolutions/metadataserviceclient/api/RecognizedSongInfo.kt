package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.rParcelableList
import de.esolutions.metadataserviceclient.util.wParcelableList
import de.esolutions.metadataserviceclient.util.wStrings

/**
 * Information about a song that was recognized by song recognition feature
 *
 * @param artist (V1) name of the songs artist, if available.
 * @param title (V1) name of the song, if available.
 * @param album (V1) name of the songs album, if available.
 * @param year (V1) year the [album] was released, if available.
 * @param coverartUri (V1) the uri that can be used to obtain the albums binary coverart data,
 *                         if available.
 * @param era (V1) era the song belongs to, if available.
 * @param genre (V1) name of the genre that best fits the song, if available.
 * @param mood (V6) textual description of the mood, if available.
 * @param tempo (V6) beats per minute of the song, if available.
 * @param externalIds (V6) list of song identifiers from external content providers like Spotify,
 *                         Amazon Music and Apple Music, if available.
 * @param gracenoteId (V7) track's Gracenote identifier, if available
 * @param gracenoteUniqueId (V7) track's unique Gracenote identifier, if available
 * @param externalAlbumIds (V8) list of album identifiers from external content providers like
 *                         Spotify, Amazon Music and Apple Music, if available.
 */
data class RecognizedSongInfo(
    val artist: String?,
    val title: String?,
    val album: String?,
    val year: String?,
    val coverartUri: String?,
    val era: String?,
    val genre: String?,
    val mood: String?,
    val tempo: String?,
    val externalIds: List<ExternalIdentifier>,
    val gracenoteId: String?,
    val gracenoteUniqueId: String?,
    val externalAlbumIds: List<ExternalIdentifier>
) : DefaultParcelable() {

    companion object {
        @JvmField
        val CREATOR = defaultCreator { parcel ->
            RecognizedSongInfo(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.rParcelableList() ?: emptyList(),
                parcel.readString(),
                parcel.readString(),
                parcel.rParcelableList() ?: emptyList()
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.wStrings(
            this.artist,
            this.title,
            this.album,
            this.year,
            this.coverartUri,
            this.era,
            this.genre,
            this.mood,
            this.tempo
        )
        dest.wParcelableList(this.externalIds)
        dest.wStrings(
            this.gracenoteId,
            this.gracenoteUniqueId
        )
        dest.wParcelableList(this.externalAlbumIds)
    }
}