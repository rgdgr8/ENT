package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for a radio station containing all known information about the station.
 *
 * @param metadata textual metadata for the station, if available.
 * @param logo already fetched default logo for the station, empty list if no logo is yet available.
 * @param logoSmall already fetched small logo for the station, intended for usage in lists,
 *                  empty list if not yet available.
 * @param streamList already fetched web streams for the station, empty list if no stream is yet
 *                available.
 * @param bearerList list of bearers that all identify the station.
 * @param providerIdentifiers identifies the content providers the station data is aggregated from.
 * @param lifeCycleData life cycle data of the station.
 */
data class StationEntity(
    var metadata: StationMetadataEntity? = null,
    var logo: ImageEntity? = null,
    var logoSmall: ImageEntity? = null,
    var streamList: StationStreamListEntity? = null,
    var bearerList: BearerListEntity? = null,
    var providerIdentifiers: List<ProviderIdentifierEntity> = listOf(),
    var lifeCycleData: LifeCycleDataEntity? = null
)

