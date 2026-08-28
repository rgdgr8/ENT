package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.end
import de.esolutions.metadataserviceclient.util.wInt
import de.esolutions.metadataserviceclient.util.wString

/**
 * Represents a provider specific identifier for some data originating from some provider.
 *
 * @param originType (V1) the origin of the provider specific identifier
 * @param id (V1) the provider specific identifier itself
 */
data class ProviderIdentifier(val originType: Int, val id: String) : DefaultParcelable() {
    companion object {
        const val ORIGIN_TYPE_RADIODNS = 1
        const val ORIGIN_TYPE_RADIOPLAYER = 2
        const val ORIGIN_TYPE_GRACENOTE = 3
        const val ORIGIN_TYPE_DTS = 4

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            ProviderIdentifier(
                parcel.readInt(),
                parcel.readString()
                    ?: throw  IllegalArgumentException(
                        "No parcel content for mandatory v1 value 'id'"
                    )
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) =
        dest.wInt(this.originType).wString(this.id).end()
}