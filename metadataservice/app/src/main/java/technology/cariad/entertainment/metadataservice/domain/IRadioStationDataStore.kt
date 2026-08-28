package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity

/**
 * CRUD interface for radio station data
 */
interface IRadioStationDataStore {

    /**
     * Checks the data store for data of the station referred to by [bearer].
     * @param bearer Requested bearer
     * @return [StationEntity] or null if the station is unknown
     */
    suspend fun getDataForBearer(bearer: IBearerEntity): StationEntity?

    /**
     * Updates the list of alternative bearers for the given bearer.
     * If alternative bearers are already stored they are replaced entirely with the new bearers.
     * @param bearer Requested bearer
     * @param bearerList
     */
    suspend fun updateBearers(bearer: IBearerEntity, bearerList: BearerListEntity)

    /**
     * Updates the metadata for the station referred to by [bearer].
     * @param bearer Requested bearer
     * @param metadata
     */
    suspend fun updateMetadata(bearer: IBearerEntity, metadata: StationMetadataEntity)

    /**
     * Updates the request and retry status for the station referred to by [bearer].
     * @param bearer Requested bearer
     * @param status
     */
    suspend fun updateLifeCycleData(bearer: IBearerEntity, lifeCycleData: LifeCycleDataEntity)

    /**
     * Updates a logo for the station referred to by [bearer].
     * A station can only have up to one normal and one small logo.
     * @param bearer Requested bearer
     * @param logo
     * @return [ImageEntity]
     */
    suspend fun updateLogo(bearer: IBearerEntity, logo: ImageEntity): ImageEntity

    /**
     *  Updates the list of streams for the station referred to by [bearer].
     *  If streams are already stored they are replaced entirely with the new streams.
     *  @param bearer Requested bearer
     *  @param streamList
     */
    suspend fun updateStreams(bearer: IBearerEntity, streamList: StationStreamListEntity)

    /**
     * Updates the list of provider identifiers for the station referred to by [bearer].
     * If providerIds are already stored they are replaced entirely with the new ids.
     * @param bearer Requested bearer
     * @param providerIdentifiers
     */
    suspend fun updateProviderIdentifiers(
        bearer: IBearerEntity,
        providerIdentifiers: List<ProviderIdentifierEntity>
    )

    /**
     * Deletes the metadata for the station referred to by [bearer].
     * @param bearer Requested bearer
     */
    suspend fun deleteMetadata(bearer: IBearerEntity)

    /**
     * Deletes the logo of the given type for the station referred to by [bearer].
     * @param bearer Requested bearer
     * @param type
     */
    suspend fun deleteLogo(bearer: IBearerEntity, type: ImageType)

    /**
     * Deletes the streams for the station referred to by [bearer].
     * @param bearer Requested bearer
     */
    suspend fun deleteStreams(bearer: IBearerEntity)
}

