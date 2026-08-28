package de.esolutions.metadataserviceclient.util

import de.esolutions.metadataserviceclient.api.StationBearer

/**
 * Base class for all typed versions of the raw [StationBearer].
 *
 * For every raw bearer type like [StationBearer.TYPE_FM_RDS] there exists a typed bearer subclass
 * of [TypedBearer] that only contains those identifier values needed for that special type. The
 * typed and raw versions can be converted into each other.
 *
 * Rule of thumb: Use the typed bearers for processing, use the raw bearer only for data transfer
 * through AIDL interface.
 */
sealed class TypedBearer {
    companion object {
        /** A typed version of the identifiers contained in the given raw [StationBearer]
         * if possible to convert. */
        fun fromRawBearer(rawBearer: StationBearer): TypedBearer? = when (rawBearer.type) {
            StationBearer.TYPE_FM_RDS -> FmRdsBearer.fromRawBearer(rawBearer)
            StationBearer.TYPE_DAB -> DabBearer.fromRawBearer(rawBearer)
            StationBearer.TYPE_IBOC -> IbocBearer.fromRawBearer(rawBearer)
            else -> null
        }
    }

    /** A raw [StationBearer] version of this [TypedBearer]. */
    abstract val rawBearer: StationBearer
}


/**
 * A [TypedBearer] for fm rds stations containing only the identifier values needed for fm rds
 * stations.
 *
 * @param pi program identifier, see [StationBearer.pi]
 * @param frequencyKhz frequency the station is aired or wildcard, see [StationBearer.frequencyKhz]
 * @param ecc european country code of the station if available, see [StationBearer.ecc]
 * @param mcc mobile country code the station was received, see [StationBearer.mcc]
 */
data class FmRdsBearer(
    val pi: String,
    val frequencyKhz: String,
    val ecc: String?,
    val mcc: String
) : TypedBearer() {
    companion object {

        /**
         * Converts raw station bearer into fm radio station bearer.
         * @param rawBearer Raw station bearer
         * @return [FmRdsBearer] if a valid rawBearer was passed, otherwise null
         */
        fun fromRawBearer(rawBearer: StationBearer): FmRdsBearer? {
            return FmRdsBearer(
                rawBearer.pi ?: return null,
                rawBearer.frequencyKhz ?: return null,
                rawBearer.ecc,
                rawBearer.mcc ?: return null
            )
        }
    }

    override val rawBearer: StationBearer by lazy {
        StationBearer(
            StationBearer.TYPE_FM_RDS,
            pi = this.pi,
            frequencyKhz = this.frequencyKhz,
            ecc = this.ecc,
            mcc = this.mcc
        )
    }
}


/**
 * A [TypedBearer] for dab stations containing only the identifier values needed for dab stations.
 *
 * @param sId service identifier, see [StationBearer.sId]
 * @param eId ensemble identifier of the stations surrounding ensemble, see [StationBearer.eId]
 * @param scIds the sub service identifier, see [StationBearer.scIds]
 * @param ecc european country code, see [StationBearer.ecc]
 */
data class DabBearer(
    val sId: String,
    val eId: String,
    val scIds: String,
    val ecc: String,
    val mcc: String
) : TypedBearer() {
    companion object {

        /**
         * Converts raw station bearer into dab station bearer.
         * @param rawBearer Raw station bearer
         * @return [DabBearer] if a valid rawBearer was passed, otherwise null
         */
        fun fromRawBearer(rawBearer: StationBearer): DabBearer? {
            return DabBearer(
                rawBearer.sId ?: return null,
                rawBearer.eId ?: return null,
                rawBearer.scIds ?: return null,
                rawBearer.ecc ?: return null,
                rawBearer.mcc ?: return null
            )
        }
    }

    override val rawBearer: StationBearer by lazy {
        StationBearer(
            StationBearer.TYPE_DAB,
            sId = this.sId,
            eId = this.eId,
            scIds = this.scIds,
            ecc = this.ecc,
            mcc = this.mcc
        )
    }
}


/**
 * A [TypedBearer] for iboc stations containing only the identifier values needed for iboc stations.
 *
 * @param tx transmitter identifier, see [StationBearer.tx]
 * @param cc iboc country code, see [StationBearer.cc]
 * @param multicastId sub program service channel if identifying an SPS, '1' if identifying the main
 *                    program service, see [StationBearer.multicastId]
 */
data class IbocBearer(val tx: String, val cc: String, val multicastId: String) : TypedBearer() {
    companion object {

        /**
         * Converts raw station bearer into iboc station bearer.
         * @param rawBearer Raw station bearer
         * @return [IbocBearer] if a valid rawBearer was passed, otherwise null
         */
        fun fromRawBearer(rawBearer: StationBearer): IbocBearer? {
            return IbocBearer(
                rawBearer.tx ?: return null,
                rawBearer.cc ?: return null,
                rawBearer.multicastId ?: return null,
            )
        }
    }

    override val rawBearer: StationBearer by lazy {
        StationBearer(
            StationBearer.TYPE_IBOC,
            tx = this.tx,
            cc = this.cc,
            multicastId = this.multicastId
        )
    }
}
