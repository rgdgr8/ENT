package technology.cariad.entertainment.metadataservice.service.radiostationdata

import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.RetrieveRadioStationLogo

class RetrieveRadioStationDataFactory(
    private val radioStationDataRepository: IRadioStationDataRepository
) {

    /**
     * Creates a new [RetrieveRadioStationLogo] use case.
     */
    fun create() = RetrieveRadioStationLogo(radioStationDataRepository)
}
