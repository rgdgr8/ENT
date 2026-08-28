package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.wInt
import de.esolutions.metadataserviceclient.util.wStrings

/**
 * A [StationBearerAsia] represents a single frequency a station is currently transmitting on.
 *
 * @param type (V5) describes the type of the station, see [StationBearerAsia.TYPE_AM] and
 *                  [StationBearerAsia.TYPE_FM]
 * @param frequencyKhz (V5) the frequency the station is aired on in kilohertz. Must be 3 or 4
 *                          decimal digits for type AM, e.g. '540', '1600', and 5 or 6 decimal
 *                          digits for type FM, e.g. '88100', '108100'.
 * @param mcc (V5) the mobile country code of the country the station was received in. Must be 3
 *                 decimal digits, e.g. '261'.
 */
data class StationBearerAsia(
    val type: Int,
    val frequencyKhz: String,
    val mcc: String
) : DefaultParcelable() {

    companion object {
        const val TYPE_AM = 1
        const val TYPE_FM = 2

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            StationBearerAsia(
                parcel.readInt(),
                parcel.readString()
                    ?: throw IllegalArgumentException(
                        "missing mandatory V5 argument 'frequencyKhz'"
                    ),
                parcel.readString()
                    ?: throw IllegalArgumentException("missing mandatory V5 argument 'mcc'"),
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.wInt(this.type)
        dest.wStrings(this.frequencyKhz, this.mcc)
    }
}
