package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * PositionData used for GracenoteService
 * latitude and longitude are given in strings such as
 * lat='39.8655', long='116.4232' for example for Bejing, China
 */
data class PositionData (
    var latitude: String? = null,
    var longitude: String? = null
)
