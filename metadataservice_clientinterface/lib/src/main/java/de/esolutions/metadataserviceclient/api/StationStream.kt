package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.end
import de.esolutions.metadataserviceclient.util.wInts
import de.esolutions.metadataserviceclient.util.wStrings

/**
 * Contains the url to a audio stream that was fetched for some [Station].
 *
 * @param url (V1) the url that can be used to obtain the streams binary data.
 * @param offsetMillis (V1) offset of the streams audio compared to live broadcast audio in
 *                          milliseconds if available.
 * @param quality (V2) the flag to indicate the quality of the stream (QUALITY_HIGH/QUALITY_LOW).
 */
data class StationStream(
    val url: String,
    val offsetMillis: Int?,
    val quality: Int?
) : DefaultParcelable() {

    companion object {
        const val QUALITY_HIGH = 1
        const val QUALITY_LOW = 2

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            StationStream(
                parcel.readString() ?: throw IllegalArgumentException("missing v1 mandatory 'url'"),
                parcel.readInt().takeIf { it != 0 },
                parcel.readInt().takeIf { it != 0 }
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) =
        dest.wStrings(this.url).wInts(this.offsetMillis ?: 0, this.quality ?: 0).end()
}