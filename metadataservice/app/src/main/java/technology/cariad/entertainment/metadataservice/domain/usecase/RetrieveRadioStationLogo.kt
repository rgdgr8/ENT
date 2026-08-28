package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataRepository
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving radio station data.
 */
class RetrieveRadioStationLogo(private val radioStationRepository: IRadioStationDataRepository) {

    /**
     * Provides radio station data.
     * @param bearer Associated station bearer
     * @return a flow of [StationEntity] including all known information about the station
     */
    fun logoByBearer(bearer: IBearerEntity, etags: ETagsEntity): Flow<StationEntity> {
        return radioStationRepository.retrieveRadioStationData(bearer, etags)
    }
}
