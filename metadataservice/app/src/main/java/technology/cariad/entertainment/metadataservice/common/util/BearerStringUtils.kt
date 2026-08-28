package technology.cariad.entertainment.metadataservice.common.util

import technology.cariad.entertainment.metadataservice.domain.entity.DabBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IbocBearerEntity

private const val FM_PI_INDEX = 0
private const val FM_FREQ_INDEX = 1
private const val FM_MCC_INDEX = 2
private const val FM_ECC_INDEX = 3
private const val DAB_EID_INDEX = 0
private const val DAB_SID_INDEX = 1
private const val DAB_SCIDS_INDEX = 2
private const val DAB_MCC_INDEX = 3
private const val DAB_ECC_INDEX = 4
private const val HD_TX_INDEX = 0
private const val HD_CC_INDEX = 1
private const val HD_MID_INDEX = 2

/**
 * Constructs a bearer key string from a bearer entity
 * If the parameters are not sent as expected, null is returned
 * Mandatory fields have to be sent
 * Serialization of parameters
 * @param  bearerEntity a bearer entity
 * @return an internally generated bearer string
 */
fun IBearerEntity.toBearerString(): String? {
    when (this) {
        is FmRdsBearerEntity -> return buildFmString(this)
        is DabBearerEntity -> return buildDabString(this)
        is IbocBearerEntity -> return buildHdString(this)
    }
    return null
}

/**
 * FM Bearer internal key string = fm:<pi>.<frequency>.<mcc>[.<ecc>]
 *  All fields are mandatory except ecc
 */
private fun buildFmString(bearer: FmRdsBearerEntity): String =
    with(bearer) {
        return if (ecc == null) {
            "fm:$pi.$frequencyKhz.$mcc"
        } else {
            "fm:$pi.$frequencyKhz.$mcc.$ecc"
        }
    }

/**
 *  DAB Bearer internal key string = dab:<eid>.<sid>.<scids>.<mcc>.<ecc>
 *  All fields are mandatory
 */
private fun buildDabString(bearer: DabBearerEntity): String? =
    with(bearer) {
        return "dab:$eId.$sId.$scIds.$mcc.$ecc"
    }

/**
 *  HD Bearer internal key string = hd:<tx>.<cc>[.<mId>]
 *  All fields are mandatory except mId
 */
private fun buildHdString(bearer: IbocBearerEntity): String =
    with(bearer) {
        return if (multicastId != null) {
            "hd:$tx.$cc.$multicastId"
        } else {
            "hd:$tx.$cc"
        }
    }

/**
 * Constructs a bearer entity from an internally generated bearer key string
 * Deserialization of parameters
 * @param  bearer an internally generated bearer string
 * @return a new bearer entity
 */
fun fromBearerString(bearer: String): IBearerEntity? {
    val tokens = bearer.split(':')

    if (tokens.size != 2) {
        return null
    }

    val params = tokens[1].split('.')

    when (tokens.first()) {
        "fm" -> return parseFmBearer(params)
        "dab" -> return parseDabBearer(params)
        "hd" -> return parseHdBearer(params)
    }

    return null
}

// fm:<pi>.<frequency>.<mcc>[.<ecc>]
private fun parseFmBearer(params: List<String>): FmRdsBearerEntity {
    val pi = params[FM_PI_INDEX]
    val freq = params[FM_FREQ_INDEX]
    val mcc = params[FM_MCC_INDEX]
    val ecc = params.elementAtOrNull(FM_ECC_INDEX)

    return FmRdsBearerEntity( pi = pi, frequencyKhz = freq, ecc = ecc,  mcc = mcc)
}

// dab:<eid>.<sid>.<scids>.<mcc>.<ecc>
private fun parseDabBearer(params: List<String>): DabBearerEntity {
    val eid = params[DAB_EID_INDEX]
    val sid = params[DAB_SID_INDEX]
    val scids = params[DAB_SCIDS_INDEX]
    val mcc = params[DAB_MCC_INDEX]
    val ecc = params[DAB_ECC_INDEX]

    return DabBearerEntity(sId = sid, eId = eid, scIds = scids, ecc = ecc, mcc = mcc)
}

// hd:<tx>.<cc>.<mId>
private fun parseHdBearer(params: List<String>): IbocBearerEntity {
    val tx = params[HD_TX_INDEX]
    val cc = params[HD_CC_INDEX]
    val mId = params.elementAtOrNull(HD_MID_INDEX) ?: "1"

    return IbocBearerEntity(tx = tx, cc = cc, multicastId = mId)
}

