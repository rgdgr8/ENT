package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.position.IPositionProvider
import technology.cariad.entertainment.metadataservice.common.util.Error
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationAsiaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RadioStationDataAsiaRepository(
    private val dataProvider: IRadioStationDataAsiaProvider,
    private val imageStore: IImageDataStore,
    private val onlineStateProvider: IOnlineStateProvider,
    private val positionProvider: IPositionProvider
) : IRadioStationDataAsiaRepository {

    private val log by logger()

    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * @see [IRadioStationDataAsiaRepository.retrieveRadioStationData]
     */
    override suspend fun retrieveRadioStationData(bearer: BearerAsiaEntity): StationAsiaEntity {
        return onlineStateProvider.onlineStateFlow(dataProvider.serviceId).filterNotNull()
            .map { onlineState ->

                if (onlineState.countryOfSales == "--") {
                    log.warn(this::retrieveRadioStationData.name) {
                        "country code not set - gracenote online functionality not available"
                    }

                    return@map StationAsiaEntity()
                }

                val position = when (val pos = positionProvider.lastPosition()) {
                    is Success -> pos.result
                    is Error -> return@map StationAsiaEntity()
                }

                dataProvider.selectNetwork(onlineState.networkInfo)
                dataProvider.setServiceConfig(onlineState.serviceListEntry)

                val station = dataProvider.getDataForBearer(bearer, position!!)
                station.logo?.let {
                    it.uri = storeLogo(it)
                }
                return@map station
            }.first()
    }

    private fun storeLogo(logo: ImageEntity): String? {
        val fileName = logo.fileName

        var uri = logo.imageData?.let {
            imageStore.saveImage(it.toByteArray(), fileName)
        }

        if (uri == null) {
            uri = imageStore.loadImageUri(fileName)
        }

        return uri
    }
}

