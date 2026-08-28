package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for dab stations containing the corresponding identifier values.
 *
 * @param sId the service identifier of the station in hex notation without '0x' prefix, e.g. 'd318'
 * @param eId  the ensemble identifier of the stations surrounding ensemble in hex notation without
 *             '0x' prefix, e.g. '10a5'
 * @param scIds the sub service identifier of the station in hex notation without '0x' prefix,
 *              e.g. '3'
 * @param ecc the european country code of the station in hex notation without '0x' prefix if
 *            available, e.g. '1a0'
 * @param mcc the mobile country code the station was received in decimal notation, e.g. '261'
 */

data class DabBearerEntity(
    val sId: String,
    val eId: String,
    val scIds: String,
    val ecc: String,
    val mcc: String
) : IBearerEntity


