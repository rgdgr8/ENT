package technology.cariad.entertainment.metadataserviceclient.api

import android.os.Parcel
import technology.cariad.entertainment.metadataserviceclient.util.DefaultParcelable
import technology.cariad.entertainment.metadataserviceclient.util.defaultCreator
import technology.cariad.entertainment.metadataserviceclient.util.rParcelable
import technology.cariad.entertainment.metadataserviceclient.util.rParcelableList
import technology.cariad.entertainment.metadataserviceclient.util.wParcelable
import technology.cariad.entertainment.metadataserviceclient.util.wParcelableList
import technology.cariad.entertainment.metadataserviceclient.util.wString

/**
 * Contains data that was fetched for some station.
 *
 * @param metadata (V1) textual metadata for the station, if available.
 * @param logo (V1) already fetched default logo for the station, null if no logo is yet available.
 * @param logoSmall (V1) already fetched small logo for the station, intended for usage in lists,
 *                       null if not yet available.
 * @param streams (V1) already fetched audio streams for the station, empty list if no stream is yet
 *                     available.
 * @param bearers (V1) list of bearers that all identify the station.
 * @param lastModified (V1) timestamp used for detecting changes of the stations data against
 *                          caches.
 * @param providerIdentifiers (V4) list of provider identifiers for the station, empty list if not
 *                                 yet available.
 */
data class Station(
    val metadata: StationMetadata?,
    val logo: StationLogo?,
    val logoSmall: StationLogo?,
    val streams: List<StationStream>,
    val bearers: List<StationBearer>,
    val lastModified: String,
    val providerIdentifiers: List<ProviderIdentifier>
) : DefaultParcelable() {

    companion object {
        @JvmField
        val CREATOR = defaultCreator { parcel ->
            Station(
                parcel.rParcelable(),
                parcel.rParcelable(),
                parcel.rParcelable(),
                parcel.rParcelableList() ?: emptyList(),
                parcel.rParcelableList() ?: emptyList(),
                parcel.readString()
                    ?: throw IllegalArgumentException("missing v1 mandatory 'lastModified'"),
                parcel.rParcelableList() ?: emptyList()
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.wParcelable(this.metadata)
        dest.wParcelable(this.logo)
        dest.wParcelable(this.logoSmall)
        dest.wParcelableList(this.streams)
        dest.wParcelableList(this.bearers)
        dest.wString(this.lastModified)
        dest.wParcelableList(this.providerIdentifiers)
    }
}
