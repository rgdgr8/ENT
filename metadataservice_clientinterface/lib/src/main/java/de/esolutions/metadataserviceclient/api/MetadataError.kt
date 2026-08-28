package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.wInt
import de.esolutions.metadataserviceclient.util.wString

/**
 * Contains data that was fetched for some station.
 *
 * @param errorType (V6) fetched logo for the station, null if no logo is available.
 * @param statusCode (V6) provider specific identifier for the station, null if not
 *                                available
 * @param errorMessage (V6) provider specific identifier for the station, null if not
 *                                available
 */
data class MetadataError(
    val errorType: Int,
    val statusCode: String?,
    val errorMessage: String?
) : DefaultParcelable() {

    companion object {

        const val NONE = 0
        const val ERROR_TYPE_IOEXCEPTION = 1
        const val ERROR_TYPE_UNEXPECTED_STATUS_CODE = 2
        const val ERROR_TYPE_TOKEN_RETRIEVAL= 3
        const val ERROR_TYPE_EMPTY_RESPONSE = 4

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            MetadataError(
                parcel.readInt(),
                parcel.readString(),
                parcel.readString(),
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.wInt(this.errorType)
        dest.wString(this.statusCode)
        dest.wString(this.errorMessage)
    }
}
