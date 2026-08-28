package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.wInt
import de.esolutions.metadataserviceclient.util.wString

/**
 * Contains data that was fetched for some station.
 *
 * @param lyricsJson (V10) The lyrics as raw JSON String in the format requested by the client.
 * @param playPositionMs (V10) The current position the track is played at in milliseconds.
 */
data class SongLyrics(
    val lyricsJson: String?,
    val playPositionMs: Int
) : DefaultParcelable() {

    companion object {

        /**
         * Complete lyrics without timing information.
         */
        const val LYRICS_TYPE_FULL = 0

        /**
         * Lyrics broken down to lines including timing information.
         */
        const val LYRICS_TYPE_LINE_BY_LINE = 1

        /**
         * Lyrics broken down to words including timing information.
         */
        const val LYRICS_TYPE_WORD_BY_WORD = 2

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            SongLyrics(
                parcel.readString(),
                parcel.readInt()
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.wString(this.lyricsJson)
        dest.wInt(this.playPositionMs)
    }
}
