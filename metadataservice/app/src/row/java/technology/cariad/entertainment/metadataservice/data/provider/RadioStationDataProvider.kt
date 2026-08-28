package technology.cariad.entertainment.metadataservice.data.provider

import de.esolutions.connectedradioclient.data.AccountManagerConfig
import de.esolutions.connectedradioclient.data.CompleteStatus
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.EndpointConfig
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.StationMetadata
import de.esolutions.connectedradioclient.data.WebStream
import de.esolutions.connectedradioclient.data.bearer.IBearer
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.handlers.media.ConRadClient
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import de.esolutions.magicadapter.stapi.AuthenticatorConstants
import de.esolutions.magicadapter.stapi.Platform
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.data.mapper.BearerEntityMapper
import technology.cariad.entertainment.metadataservice.data.mapper.BearerMapper
import technology.cariad.entertainment.metadataservice.data.mapper.ProviderIdentifierMapper
import technology.cariad.entertainment.metadataservice.data.mapper.RetryBehaviorMapper
import technology.cariad.entertainment.metadataservice.data.mapper.StationLogoMapper
import technology.cariad.entertainment.metadataservice.data.mapper.StationMetadataMapper
import technology.cariad.entertainment.metadataservice.data.mapper.StationStreamMapper
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.StationPart
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity
import technology.cariad.entertainment.metadataservice.domain.value.ConRadTypeId
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RadioStationDataProvider(
    override val serviceId: ServiceIdentifier,
    private val platform: Platform,
    private val conRadClient: ConRadClient
) : IRadioStationDataProvider {

    private val log by logger()

    private val conRadTransactionTypeId: String
    private val conRadOperationTypeId: String
    private val optionServiceId: String
    private val optionOperationId: String

    init {
        when (platform) {
            Platform.OI -> {
                conRadTransactionTypeId = ConRadTypeId.TRANSACTION_TYPEID.typeId
                conRadOperationTypeId = ConRadTypeId.OPERATION_TYPEID.typeId
                optionServiceId = AuthenticatorConstants.BUNDLE_OPTIONS_TRANSACTIONTYPEID
                optionOperationId = AuthenticatorConstants.BUNDLE_OPTIONS_OPERATIONTYPEID
            }

            Platform.HCP3 -> {
                conRadTransactionTypeId = serviceId.serviceName
                conRadOperationTypeId = AuthenticatorConstants.BUNDLE_OPTIONS_OPERATIONID_DEFAULT
                optionServiceId = AuthenticatorConstants.BUNDLE_OPTIONS_SERVICEID
                optionOperationId = AuthenticatorConstants.BUNDLE_OPTIONS_OPERATIONID
            }
        }
    }

    /**
     * @see [IRadioStationDataProvider.getDataForBearer]
     */
    override suspend fun getDataForBearer(
        bearer: IBearerEntity, eTags: ETagsEntity?
    ): List<Pair<StationPart, Any?>> =
        suspendCoroutine { cont ->

            val stationParts = mutableListOf<Pair<StationPart, Any?>>()

            val callback = object : IRadioStationDataCallback {

                override fun onBearers(etag: String?, bearers: List<IBearer>?) {
                    val bearerEntities =
                        bearers?.let { BearerEntityMapper().mapList(it) } ?: listOf()

                    val bearerListEntity = BearerListEntity(bearerEntities, etag)
                    stationParts.add(StationPart.BEARER to bearerListEntity)

                    log.info(this::onBearers.name) {
                        "received ${bearers?.size} alternative bearers for " +
                                "bearer=$bearer etag=$etag"
                    }
                }

                override fun onMetadata(etag: String?, metadata: StationMetadata?) {
                    val metadataEntity = metadata?.let { StationMetadataMapper().mapValue(it) }
                    metadataEntity?.etag = etag
                    stationParts.add(StationPart.METADATA to metadataEntity)

                    val providerIdentifierEntities =
                        metadata?.let { ProviderIdentifierMapper().mapList(it.providerIdentifiers) }
                    stationParts.add(StationPart.PROVIDER_IDENTIFIERS to providerIdentifierEntities)

                    log.info(this::onMetadata.name) {
                        "received $metadata for bearer=$bearer etag=$etag"
                    }
                }

                override fun onStreams(etag: String?, streams: List<WebStream>?) {
                    val streamEntities =
                        streams?.let { StationStreamMapper().mapList(it) } ?: listOf()
                    val streamListEntity = StationStreamListEntity(streamEntities, etag)
                    stationParts.add(StationPart.STREAM to streamListEntity)

                    log.info(this::onStreams.name) {
                        "received ${streams?.size} streams for bearer=$bearer etag=$etag"
                    }
                }

                override fun onLogo(etag: String?, logo: StationLogo?) {
                    val logoEntity = logo?.let { StationLogoMapper().mapValue(it) }
                    logoEntity?.imageType = ImageType.LOGO
                    logoEntity?.eTag = etag
                    stationParts.add(StationPart.LOGO to logoEntity)

                    log.info(this::onLogo.name) {
                        "received logo for bearer=$bearer " +
                                "logo=$logoEntity size=${logoEntity?.imageData?.size}B etag=$etag"
                    }
                }

                override fun onSLogo(etag: String?, logo: StationLogo?) {
                    val logoSmallEntity = logo?.let { StationLogoMapper().mapValue(it) }
                    logoSmallEntity?.imageType = ImageType.LOGO_SMALL
                    logoSmallEntity?.eTag = etag
                    stationParts.add(StationPart.LOGO_SMALL to logoSmallEntity)

                    log.info(this::onSLogo.name) {
                        "received small logo for bearer=$bearer " +
                                "logo=$logoSmallEntity size=${logoSmallEntity?.imageData?.size}B " +
                                "etag=$etag"
                    }
                }

                override fun onComplete(status: CompleteStatus, expirationDate: Long?) {
                    val lifeCycleData = LifeCycleDataEntity(
                        expirationDate = expirationDate,
                        retryStrategy = RetryBehaviorMapper().mapValue(status.retry),
                        retryDelay = status.retryDelaySeconds
                    )

                    stationParts.add(StationPart.LIFE_CYCLE_DATA to lifeCycleData)

                    if (status.retry == RetryBehavior.NONE) {
                        log.info(this::onComplete.name) {
                            "station update completed, expiration=$expirationDate"
                        }
                    } else {
                        log.warn(this::onComplete.name) {
                            "station update failed, retry=${status.retry} " +
                                    "delay=${status.retryDelaySeconds}"
                        }
                    }

                    cont.resume(stationParts)
                }
            }

            conRadClient.getRadioStationData(
                BearerMapper().mapValue(bearer),
                ETags(
                    eTags?.metadataETag,
                    eTags?.bearerListETag,
                    eTags?.streamsETag,
                    eTags?.logoETag,
                    eTags?.logoSmallETag
                ),
                callback
            )
        }

    /**
     * @see [IRadioStationDataProvider.selectNetwork]
     */
    override fun selectNetwork(networkInfo: NetworkInfo?) {
        log.info(this::selectNetwork.name) {
            "set network to: networkInfo=$networkInfo"
        }

        conRadClient.setNetwork(networkInfo?.network)
    }

    /**
     * @see [IRadioStationDataProvider.setServiceConfig]
     */
    override fun setServiceConfig(serviceListEntry: ServiceListEntry?) {
        log.info(this::setServiceConfig.name) {
            "service list changed: serviceListEntry=$serviceListEntry"
        }

        // Only configure conRadClient if we have a valid URL
        // This prevents the race condition where Config.endpointConfig is set with empty URL
        val httpOperation = serviceListEntry?.defaultHttpOperation
        val url = httpOperation?.url
        if (url.isNullOrEmpty()) {
            log.info(this::setServiceConfig.name) {
                "skipping config: no valid URL in defaultHttpOperation"
            }
            return
        }

        val accountManagerConfig = AccountManagerConfig(
            serviceId = conRadTransactionTypeId,
            accountType = AuthenticatorConstants.CORESERVICE_ACCOUNT_TYPE,
            accountName = AuthenticatorConstants.CORESERVICE_ACCOUNT_NAME,
            optionServiceId = optionServiceId,
            optionOperationId = optionOperationId,
        )

        val endpointConfig = EndpointConfig(
            url,
            conRadOperationTypeId
        )

        log.info(this::setServiceConfig.name) {
            "set config to: " +
                    "accountManagerConfig=$accountManagerConfig " +
                    "endpointConfig=$endpointConfig"
        }

        conRadClient.setConfig(accountManagerConfig, endpointConfig)
    }
}

