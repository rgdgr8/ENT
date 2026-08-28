package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.end
import de.esolutions.metadataserviceclient.util.wStrings

/**
 * Request data needed to ask for a cover art image.
 *
 * Not all possible combinations of these three parameters are suitable to uniquely identify an
 * album and thus find a matching cover art.
 * Providing only an artist or only a song's title is oftentimes ambiguous and therefore not
 * supported.
 *
 * @param artist (V1) the artist of the album the cover art should be requested for, if available.
 * @param title (V1) the title contained in the album the cover art should be requested for, if
 *                   available.
 * @param album (V1) the name of the album the cover art should be requested for, if available.
 */
data class CoverArtRequestData(val artist: String?, val title: String?, val album: String?) :
    DefaultParcelable() {
    companion object {
        @JvmField
        val CREATOR = defaultCreator { parcel ->
            CoverArtRequestData(
                parcel.readString(),
                parcel.readString(),
                parcel.readString()
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) =
        dest.wStrings(this.artist, this.title, this.album).end()
}