package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.rParcelable
import de.esolutions.metadataserviceclient.util.wParcelable
import de.esolutions.metadataserviceclient.util.wString

/**
 * Contains data that was fetched for some station.
 *
 * @param content (V6) fetched name of the station, null if no name is available
 * @param error (V6) fetched logo for the station, null if no logo is available.
 */
data class MusicMetadata(
    val content: String?,
    val error: MetadataError?
) : DefaultParcelable() {

    companion object {
        @JvmField
        val CREATOR = defaultCreator { parcel ->
            MusicMetadata(
                parcel.readString(),
                parcel.rParcelable()
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.wString(this.content)
        dest.wParcelable(this.error)
    }
}
