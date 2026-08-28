package technology.cariad.entertainment.metadataserviceclient.api

import android.os.Parcel
import technology.cariad.entertainment.metadataserviceclient.util.DefaultParcelable
import technology.cariad.entertainment.metadataserviceclient.util.defaultCreator
import technology.cariad.entertainment.metadataserviceclient.util.rParcelable
import technology.cariad.entertainment.metadataserviceclient.util.wParcelable
import technology.cariad.entertainment.metadataserviceclient.util.wString

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

