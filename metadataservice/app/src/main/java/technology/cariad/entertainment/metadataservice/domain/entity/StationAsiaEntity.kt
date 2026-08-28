package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for an asia radio station containing all known information about the station.
 *
 * @param name the name of the station, if available.
 * @param logo already fetched default logo for the station, empty list if no logo is yet available.
 * @param providerIdentifier identifies the content provider the station data is aggregated from.
 */
data class StationAsiaEntity(
    val name: String? = null,
    val logo: ImageEntity? = null,
    val providerIdentifier: ProviderIdentifierEntity? = null
)

