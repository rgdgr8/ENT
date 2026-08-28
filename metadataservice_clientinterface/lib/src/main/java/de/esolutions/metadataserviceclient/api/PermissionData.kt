package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.end
import de.esolutions.metadataserviceclient.util.rIntList
import de.esolutions.metadataserviceclient.util.wIntList

/**
 * Represents the current permissions status.
 *
 * @param missingPermissions (V1) list of the identifiers for the missing permissions
 */
data class PermissionData(val missingPermissions: List<Int>) : DefaultParcelable() {

    companion object {

        /**
         * Flag corresponding to the dangerous permission android.permission.RECORD_AUDIO.
         * @note This flag is currently not used.
         */
        const val PERMISSION_RECORD_AUDIO = 1

        /**
         * Flag corresponding to both dangerous permissions android.permission.ACCESS_FINE_LOCATION
         * and android.permission.ACCESS_BACKGROUND_LOCATION.
         * If this flag is present either one or both of these permissions have not been granted.
         */
        const val PERMISSION_FINE_BACKGROUND_LOCATION = 2

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            PermissionData(
                parcel.rIntList()
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) =
        dest.wIntList(this.missingPermissions).end()
}