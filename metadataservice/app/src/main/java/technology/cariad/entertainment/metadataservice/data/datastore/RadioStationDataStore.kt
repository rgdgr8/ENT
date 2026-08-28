package technology.cariad.entertainment.metadataservice.data.datastore

import androidx.room.withTransaction
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.fromBearerString
import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.BearerStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.GenreStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.ImageStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.LifeCycleDataStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.MetadataStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.ProviderIdentifierStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.StationStore
import technology.cariad.entertainment.metadataservice.data.datastore.radiostation.StreamStore
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity
import java.io.IOException
import java.util.TreeMap

private const val INVALID_DATA_INDEX = -1L

@Suppress("LongParameterList")
class RadioStationDataStore(
    private val database: CachingDatabase,
    private val bearerStore: BearerStore,
    private val stationStore: StationStore,
    private val metadataStore: MetadataStore,
    private val genreStore: GenreStore,
    private val imageStore: ImageStore,
    private val imageDataStore: IImageDataStore,
    private val streamStore: StreamStore,
    private val providerIdentifierStore: ProviderIdentifierStore,
    private val lifeCycleDataDataStore: LifeCycleDataStore,
    private val timeProvider: ITimeProvider
) : IRadioStationDataStore {

    private val log by logger()

    /**
     * @see [IRadioStationDataStore.updateBearers]
     */
    override suspend fun updateBearers(bearer: IBearerEntity, bearerList: BearerListEntity) =
        database.withTransaction {
            val stationId = stationStore.getOrCreateStation(bearer)
            if (stationId != INVALID_DATA_INDEX) {
                bearerStore.updateBearers(stationId, bearerList)
            }
        }

    /**
     * @see [IRadioStationDataStore.updateMetadata]
     */
    override suspend fun updateMetadata(bearer: IBearerEntity, metadata: StationMetadataEntity) =
        database.withTransaction {
            val stationId = stationStore.getOrCreateStation(bearer)

            if (stationId != INVALID_DATA_INDEX) {
                metadataStore.updateMetadata(stationId, metadata)
                genreStore.updateGenres(stationId, metadata.genres ?: listOf())
            }
        }

    /**
     * @see [IRadioStationDataStore.updateLifeCycleData]
     */
    override suspend fun updateLifeCycleData(
        bearer: IBearerEntity,
        lifeCycleData: LifeCycleDataEntity
    ) {
        database.withTransaction {
            val stationId = stationStore.getOrCreateStation(bearer)
            if (stationId != INVALID_DATA_INDEX) {
                lifeCycleDataDataStore.updateLifeCycleData(stationId, lifeCycleData)
            }
        }
    }

    /**
     * @see [IRadioStationDataStore.updateLogo]
     */
    override suspend fun updateLogo(bearer: IBearerEntity, logo: ImageEntity): ImageEntity =
        database.withTransaction {
            val stationId = stationStore.getOrCreateStation(bearer)
            if (stationId != INVALID_DATA_INDEX) {
                logo.uri = logo.imageData?.let {
                    imageDataStore.saveImage(it.toByteArray(), logo.fileName)
                } ?: imageDataStore.loadImageUri(logo.fileName)
                imageStore.updateLogo(stationId, logo)
                logo.imageData = null
            }
            return@withTransaction logo
        }

    /**
     * @see [IRadioStationDataStore.updateStreams]
     */
    override suspend fun updateStreams(bearer: IBearerEntity, streamList: StationStreamListEntity) =
        database.withTransaction {
            val stationId = stationStore.getOrCreateStation(bearer)
            if (stationId != INVALID_DATA_INDEX) {
                streamStore.updateStreams(stationId, streamList)
            }
        }

    /**
     * @see [IRadioStationDataStore.updateProviderIdentifiers]
     */
    override suspend fun updateProviderIdentifiers(
        bearer: IBearerEntity,
        providerIdentifiers: List<ProviderIdentifierEntity>
    ) {
        database.withTransaction {
            val stationId = stationStore.getOrCreateStation(bearer)
            if (stationId != INVALID_DATA_INDEX) {
                providerIdentifierStore.updateProviderIdentifiers(stationId, providerIdentifiers)
            }
        }
    }

    /**
     * @see [IRadioStationDataStore.deleteMetadata]
     */
    override suspend fun deleteMetadata(bearer: IBearerEntity) {
        database.withTransaction {
            val stationId = bearerStore.getStationIdByBearer(bearer) ?: return@withTransaction

            metadataStore.deleteByStationId(stationId)
        }
    }

    /**
     * @see [IRadioStationDataStore.deleteLogo]
     */
    override suspend fun deleteLogo(bearer: IBearerEntity, type: ImageType) {
        try {
            database.withTransaction {
                val stationId = bearerStore.getStationIdByBearer(bearer) ?: return@withTransaction

                val imageId =
                    imageStore.findLogoByStationIdAndType(stationId, type) ?: return@withTransaction
                val imageEntry = imageStore.getImageEntryById(imageId) ?: return@withTransaction
                val stationImageEntry = imageStore.createStationImageEntry(
                    imageId = imageId,
                    stationId = stationId
                )
                imageStore.delete(stationImageEntry, type)

                val imageIds = imageStore.getImageIdsByImageName(imageEntry.fileName)
                if (imageIds.isEmpty()) {
                val success = imageDataStore.deleteImage(imageEntry.fileName)
                if (!success) {
                    throw IOException("failed to delete ${imageEntry.fileName}")
                }
                } else {
                    log.info(this::deleteLogo.name) {
                        "Image: ${imageEntry.fileName}  not deleted because it is used by " +
                                "these image ids: $imageIds"
                    }
                }
            }
        } catch (e: IOException) {
            log.error(this::deleteLogo.name) {
                "${e.message}"
            }
        }
    }

    /**
     * @see [IRadioStationDataStore.deleteStreams]
     */
    override suspend fun deleteStreams(bearer: IBearerEntity) {
        database.withTransaction {
            val stationId = bearerStore.getStationIdByBearer(bearer) ?: return@withTransaction
            streamStore.deleteByStationId(stationId)
        }
    }

    /**
     * @see [IRadioStationDataStore.getDataForBearer]
     */
    override suspend fun getDataForBearer(bearer: IBearerEntity): StationEntity? {
        return database.withTransaction {
            bearer.toBearerString() ?: return@withTransaction null
            val station = getRadioStation(bearer)

            station?.lifeCycleData?.let {
                it.lastRequested = timeProvider.secondsSinceEpoch()
                updateLifeCycleData(bearer, it)
            }

            station
        }
    }

    private suspend fun getRadioStation(bearer: IBearerEntity): StationEntity? {

        val stationId = bearerStore.getStationIdByBearer(bearer) ?: return null

        val station = stationStore.getCompleteById(stationId) ?: return null

        val genreRankMap = station.genreRanks.associateBy { it.genreId }
        val sortedGenreMap = TreeMap<Int, String>()
        station.genres.forEach { entry ->
            genreRankMap[entry.genreId]?.position?.let {
                sortedGenreMap[it] = entry.name
            }
        }

        val metadata = station.stationMetadata?.let {
            StationMetadataEntity(
                shortName = it.shortName,
                mediumName = it.mediumName,
                longName = it.longName,
                description = it.description,
                genres = sortedGenreMap.values.toList(),
                etag = station.etags?.metadataETag
            )
        }

        val logo = station.images.elementAtOrNull(0)?.let {
            ImageEntity(
                uri = imageDataStore.loadImageUri(it.fileName),
                fileName = it.fileName,
                imageType = ImageType.LOGO,
                imageData = null,
                eTag = station.etags?.logoETag
            )
        }

        val logoSmall = station.images.elementAtOrNull(1)?.let {
            ImageEntity(
                uri = imageDataStore.loadImageUri(it.fileName),
                fileName = it.fileName,
                imageType = ImageType.LOGO_SMALL,
                imageData = null,
                eTag = station.etags?.logoSmallETag
            )
        }

        val streams = station.streams
            .sortedBy { it.position }
            .map {
                StationStreamEntity(
                    url = it.url,
                    offsetMillis = it.offsetMs,
                    quality = it.quality
                )
            }

        val streamList = StationStreamListEntity(streams, station.etags?.streamsETag)

        val bearers =  station.bearers.mapNotNull {
            fromBearerString(it.uri)
        }
        val bearerList = BearerListEntity(bearers, station.etags?.bearerListETag)

        return StationEntity(
            metadata,
            logo,
            logoSmall,
            streamList,
            bearerList,
            station.providerIdentifiers.map {
                ProviderIdentifierEntity(
                    originType = it.origin,
                    id = it.externalId
                )
            },
            LifeCycleDataEntity(
                lastRequested = station.lifeCycleData.lastRequested,
                expirationDate = station.lifeCycleData.validUntil,
                retryDelay = station.lifeCycleData.retryDelay,
                retryCount = station.lifeCycleData.retryCount,
                retryStrategy = station.lifeCycleData.lastRetryStatus,
                retryAfter = station.lifeCycleData.nextRetry
            )
        )
    }
}

