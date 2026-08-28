package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import kotlinx.coroutines.flow.Flow

interface IRadioStationDataRepository {

    /**
     * @exception [technology.cariad.entertainment.metadataservice.common.util.ServiceDisabledException] is
     * thrown is a service entry is missing or a disable reason exists
     */
    fun retrieveRadioStationData(bearer: IBearerEntity, etags: ETagsEntity): Flow<StationEntity>
}
