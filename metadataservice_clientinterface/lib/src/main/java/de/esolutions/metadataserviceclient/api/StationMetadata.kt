package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.end
import de.esolutions.metadataserviceclient.util.wStringList
import de.esolutions.metadataserviceclient.util.wStrings

/**
 * Contains metadata that was fetched for some [Station].
 *
 * @param shortName (V1) the providers short name for the station, if available.
 * @param mediumName (V1) the providers medium name for the station, if available.
 * @param longName (V1) the providers longName name for the station, if available.
 * @param description (V1) the providers description text for the station, if available.
 * @param genres (V1) the providers genre names for the station, ordered from most to least
 *                    important, if available.
 */
data class StationMetadata(
    val shortName: String?,
    val mediumName: String?,
    val longName: String?,
    val description: String?,
    val genres: List<String>?
) : DefaultParcelable() {

    companion object {
        @JvmField
        val CREATOR = defaultCreator { parcel ->
            StationMetadata(
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                mutableListOf<String>().apply { parcel.readStringList(this) }
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) =
        dest.wStrings(this.shortName, this.mediumName, this.longName, this.description)
            .wStringList(this.genres).end()
}