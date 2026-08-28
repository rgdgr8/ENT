package technology.cariad.entertainment.metadataservice.service.radiostationasia

import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.domain.usecase.RetrieveRadioStationAsia

class RetrieveRadioStationAsiaFactory(
    private val radioStationDataRepository: IRadioStationDataAsiaRepository
) {
    /**
     * Creates a new [RetrieveRadioStationAsia] use case.
     */
    fun create() = RetrieveRadioStationAsia(radioStationDataRepository)
}

