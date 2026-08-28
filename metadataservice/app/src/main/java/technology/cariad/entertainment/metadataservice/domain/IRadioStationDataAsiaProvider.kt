package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.common.position.PositionInformation
import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationAsiaEntity

interface IRadioStationDataAsiaProvider : IOnlineService {

    /**
     * Provides all known information about the station.
     * @param bearer associated bearer with the station
     * @param position current geo location
     * @return [StationAsiaEntity] containing available station's data
     */
    suspend fun getDataForBearer(
        bearer: BearerAsiaEntity,
        position: PositionInformation
    ): StationAsiaEntity
}

