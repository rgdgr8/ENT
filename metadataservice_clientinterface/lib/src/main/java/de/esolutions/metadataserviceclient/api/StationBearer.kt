package de.esolutions.metadataserviceclient.api

import android.os.Parcel
import de.esolutions.metadataserviceclient.util.DefaultParcelable
import de.esolutions.metadataserviceclient.util.defaultCreator
import de.esolutions.metadataserviceclient.util.wStrings

/**
 * A [StationBearer] is a bunch of identifier values used to identify a single station or a whole
 * network of stations. Note that only a few of all the possible identifier values is used
 * depending on the [type] of station that should be identified. So most of them are optional.
 * Which values are used in which cases is described at the start of each parameter descriptions.
 *
 * @param type (V1) describes the type of station or network the [StationBearer] identifies.
 *                  One of [StationBearer.TYPE_FM_RDS], [StationBearer.TYPE_DAB],
 *                  [StationBearer.TYPE_IBOC]
 * @param pi (V1, FM_RDS) contains the program identifier of the station. Must be 4 hexadecimal
 *                        digits without '0x' prefix, e.g. '16b0'.
 * @param frequencyKhz (V1, FM_RDS) the frequency the station is aired on or a wildcard when don't
 *                                  care. If a frequency is set it must consist of 5 decimal digits.
 *                                  Frequencies below 100MHz must be padded with a leading '0'.
 *                                  e.g. '10490', '09990', or '*'.
 * @param sId (V1, DAB) the service identifier of the station. Must be 4 hexadecimal digits without
 *                      '0x' prefix, e.g. 'd318'.
 * @param eId (V1, DAB) the ensemble identifier of the stations surrounding ensemble. Must be 4
 *                      hexadecimal digits without '0x' prefix, e.g. '10a5'.
 * @param scIds (V1, DAB) the sub service identifier of the station. Must be 1 hexadecimal digit
 *                        without '0x' prefix, e.g. '3'.
 * @param ecc (V1, FM_RDS and DAB) the extended country code of the station, if available. Must be
 *                                 2 hexadecimal digits without '0x' prefix, e.g. 'a0'.
 * @param tx (V1, IBOC) the transmitter identifier of the station. Must be 5 hexadecimal digits
 *                      without '0x' prefix, e.g. '021f8'.
 * @param cc (V1, IBOC) the iboc country code of the station. Must be 3 hexadecimal digits without
 *                      '0x' prefix, e.g. '292'.
 * @param multicastId (V1, IBOC) the identifier for a multicast sub program service channel. Must be
 *                               1 hexadecimal digit without '0x' prefix, e.g. '2'. For the main
 *                               program service the id must be '1'.
 * @param mcc (V1, FM_RDS and DAB) the mobile country code of the country the station was received
 *                                 in. Must be 3 decimal digits, e.g. '261'.
 */
data class StationBearer(
    val type: Int,
    val pi: String? = null,
    val frequencyKhz: String? = null,
    val sId: String? = null,
    val eId: String? = null,
    val scIds: String? = null,
    val ecc: String? = null,
    val tx: String? = null,
    val cc: String? = null,
    val multicastId: String? = null,
    val mcc: String? = null
) : DefaultParcelable() {

    companion object {
        const val TYPE_FM_RDS = 1
        const val TYPE_DAB = 2
        const val TYPE_IBOC = 3

        @JvmField
        val CREATOR = defaultCreator { parcel ->
            StationBearer(
                parcel.readInt(),
                parcel.readString(), parcel.readString(),
                parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(),
                parcel.readString(), parcel.readString(), parcel.readString(),
                parcel.readString()
            )
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.type)
        dest.wStrings(this.pi, this.frequencyKhz)
        dest.wStrings(this.sId, this.eId, this.scIds, this.ecc)
        dest.wStrings(this.tx, this.cc, this.multicastId)
        dest.wStrings(this.mcc)
    }
}