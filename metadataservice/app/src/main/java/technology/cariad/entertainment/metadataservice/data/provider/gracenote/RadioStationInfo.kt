package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * Radio station data for GracenoteService
 *
 * - when used as input for requesting radiostation metadata Frequency and Band need to be set
 *   the Frequency should represent the value given in kHz, e.g. 82200 for 82.2 MHz FM,
 *   band can either be 'am' or 'fm' either upper case or lower case strings
 *
 * - when given as a result in the callback the values Name and StationId are filled with the
 *   corresponding values found by GracenoteService
 */
@SuppressWarnings("ConstructorParameterNaming")
data class RadioStationInfo(
    var Name: String? = null,
    var Band: String? = null,
    var Frequency: String? = null,
    var StationId: String? = null
) {

    //generated
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RadioStationInfo

        if (Band != other.Band) return false
        if (Frequency != other.Frequency) return false

        return true
    }

    //generated
    override fun hashCode(): Int {
        var result = Band?.hashCode() ?: 0
        result = 31 * result + (Frequency?.hashCode() ?: 0)
        return result
    }
}
