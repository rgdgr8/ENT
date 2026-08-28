package technology.cariad.entertainment.metadataserviceclient.api

import android.os.Parcel
import technology.cariad.entertainment.metadataserviceclient.util.DefaultParcelable
import technology.cariad.entertainment.metadataserviceclient.util.defaultCreator
import technology.cariad.entertainment.metadataserviceclient.util.end
import technology.cariad.entertainment.metadataserviceclient.util.wInts
import technology.cariad.entertainment.metadataserviceclient.util.wStrings

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
