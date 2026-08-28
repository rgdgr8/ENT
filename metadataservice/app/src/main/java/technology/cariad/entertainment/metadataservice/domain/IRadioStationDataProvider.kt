package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity

enum class StationPart {
    BEARER,
    LOGO,
    LOGO_SMALL,
    METADATA,
    STREAM,
    PROVIDER_IDENTIFIERS,
    LIFE_CYCLE_DATA
}

interface IRadioStationDataProvider : IOnlineService {

    /**
     * Retrieves radio station data from an online data provider.
     * @param bearer associated bearer with the station
     * @param eTags eTags send with a previous update for the same station
     * @return a list including pairs of [StationPart] and respective retrieved data
     */
    suspend fun getDataForBearer(
        bearer: IBearerEntity,
        eTags: ETagsEntity?
    ): List<Pair<StationPart, Any?>>
}

