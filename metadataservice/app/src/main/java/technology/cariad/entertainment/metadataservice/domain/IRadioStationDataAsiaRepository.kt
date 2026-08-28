package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationAsiaEntity

interface IRadioStationDataAsiaRepository {

    /**
     * @exception [technology.cariad.entertainment.metadataservice.common.util.ServiceDisabledException] is
     * thrown is a service entry is missing or a disable reason exists
     */
    suspend fun retrieveRadioStationData(bearer: BearerAsiaEntity): StationAsiaEntity
}
