package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataRepository
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RadioStationDataRepository(
    dataProvider: IRadioStationDataProvider,
    dataStore: IRadioStationDataStore,
    timeProvider: ITimeProvider,
    onlineStateProvider: IOnlineStateProvider
) : IRadioStationDataRepository {

    private val log by logger()

    override fun retrieveRadioStationData(
        bearer: IBearerEntity, etags: ETagsEntity
    ): Flow<StationEntity> = flow {

        log.error(this@RadioStationDataRepository::retrieveRadioStationData.name) {
            "Feature is not available in this region."
        }

        throw NotImplementedError("Feature is not available in this region.");
    }
}

