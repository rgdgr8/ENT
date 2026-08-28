package technology.cariad.entertainment.metadataserviceclient.api

import android.os.Parcel
import technology.cariad.entertainment.metadataserviceclient.util.DefaultParcelable
import technology.cariad.entertainment.metadataserviceclient.util.defaultCreator
import technology.cariad.entertainment.metadataserviceclient.util.end
import technology.cariad.entertainment.metadataserviceclient.util.wString

/**
 * Contains the uri to a logo that was fetched for some [Station].
 *
 * @param uri (V1) the uri that can be used to obtain the logos binary data.
 */
data class StationLogo(val uri: String) : DefaultParcelable() {

    companion object {
        @JvmField
        val CREATOR = defaultCreator { parcel ->
            StationLogo(
                parcel.readString() ?: throw IllegalArgumentException("missing v1 mandatory 'uri'")
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) = dest.wString(this.uri).end()
}
