package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for fm rds stations containing the corresponding identifier values.
 *
 * @param pi contains the program identifier of the station in hex notation without '0x' prefix,
 *           e.g. '16b0'
 * @param frequencyKhz  the frequency the station is aired or wildcard when don't care,
 *                      e.g. '09990' or '*'
 * @param ecc the european country code of the station in hex notation without '0x' prefix
 *            if available, e.g. '1a0'
 * @param mcc the mobile country code the station was received in decimal notation, e.g. '261'
 */
data class FmRdsBearerEntity(
    val pi: String,
    val frequencyKhz: String,
    val ecc: String? = null,
    val mcc: String
) : IBearerEntity

