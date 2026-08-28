package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationAsiaEntity

/**
 * Use case for retrieving radio station data in asia.
 */
class RetrieveRadioStationAsia(
    private val radioStationRepository: IRadioStationDataAsiaRepository
) {
    /**
     * Provide radio station data for asia.
     * @param bearer Associated station bearer
     * @return [StationAsiaEntity] all known information regarding the station
     */
    suspend fun dataByBearer(bearer: BearerAsiaEntity): StationAsiaEntity {
        return radioStationRepository.retrieveRadioStationData(bearer)
    }
}

