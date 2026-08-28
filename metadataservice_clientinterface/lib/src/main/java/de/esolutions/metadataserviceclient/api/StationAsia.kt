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
 * @param name (V5) fetched name of the station, null if no name is available
 * @param logo (V5) fetched logo for the station, null if no logo is available.
 * @param providerIdentifier (V5) provider specific identifier for the station, null if not
 *                                available
 */
data class StationAsia(
    val name: String?,
    val logo: StationLogo?,
    val providerIdentifier: ProviderIdentifier?
) : DefaultParcelable() {

    companion object {
        @JvmField
        val CREATOR = defaultCreator { parcel ->
            StationAsia(
                parcel.readString(),
                parcel.rParcelable(),
                parcel.rParcelable(),
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.wString(this.name)
        dest.wParcelable(this.logo)
        dest.wParcelable(this.providerIdentifier)
    }
}
