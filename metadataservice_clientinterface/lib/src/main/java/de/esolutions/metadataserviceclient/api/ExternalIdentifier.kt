package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.end
import de.esolutions.metadataserviceclient.util.wInt
import de.esolutions.metadataserviceclient.util.wString

/**
 * Represents an external identifier linking to contents from third parties.
 *
 * @param originType (V6) the origin of the external identifier. Must be one of
 * [ORIGIN_TYPE_APPLE], [ORIGIN_TYPE_AMAZON], [ORIGIN_TYPE_SPOTIFY].
 * @param id (V6) the external identifier itself
 */
data class ExternalIdentifier(val originType: Int, val id: String) : DefaultParcelable() {
    companion object {
        const val ORIGIN_TYPE_APPLE = 1
        const val ORIGIN_TYPE_AMAZON = 2
        const val ORIGIN_TYPE_SPOTIFY = 3

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            ExternalIdentifier(
                parcel.readInt(),
                parcel.readString()
                    ?: throw  IllegalArgumentException(
                        "No parcel content for mandatory v6 value 'id'"
                    )
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) =
        dest.wInt(this.originType).wString(this.id).end()
}
