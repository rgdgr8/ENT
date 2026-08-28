package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.end
import de.esolutions.metadataserviceclient.util.wString

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