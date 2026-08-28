package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for iboc stations containing the corresponding identifier values.
 *
 * @param tx the transmitter identifier of the station in hex notation without '0x' prefix,
 *           e.g. '021f8'
 * @param cc the iboc country code of the station in hex notation without '0x' prefix e.g. '292'
 * @param multicastId the sub program service channel if identifying an SPS, '1' for the main
 *                    program service
 */
data class IbocBearerEntity(
    val tx: String,
    val cc: String,
    val multicastId: String
) : IBearerEntity


