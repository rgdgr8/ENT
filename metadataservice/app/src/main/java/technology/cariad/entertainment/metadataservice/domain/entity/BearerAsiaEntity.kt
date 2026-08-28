package technology.cariad.entertainment.metadataservice.domain.entity

enum class BearerAsiaType {
    AM,
    FM
}

/**
 * Bearer entity class for asia radio stations containing the corresponding identifier values.
 *
 * @param type the type of the station.
 * @param frequencyKhz  the frequency the station is aired e.g. '09990'
 * @param mcc the mobile country code the station was received in decimal notation, e.g. '261'
 */
data class BearerAsiaEntity(
    val type: BearerAsiaType,
    val frequencyKhz: String,
    val mcc: String
)

