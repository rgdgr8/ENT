package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.domain.ICoverArtDataStore
import technology.cariad.entertainment.metadataservice.domain.ICoverArtProvider
import technology.cariad.entertainment.metadataservice.domain.ICoverArtRepository
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.LookupMode
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class CoverArtRepository(
    private val dataProvider: ICoverArtProvider,
    private val dataStore: ICoverArtDataStore,
    private val onlineStateProvider: IOnlineStateProvider,
    private val timeProvider: ITimeProvider
) : ICoverArtRepository {

    companion object {
        private const val ONLINE_STATE_WAIT_TIMEOUT_MS = 1200L
    }

    private data class ProviderConfig(
        val networkInfo: NetworkInfo?,
        val serviceListEntry: ServiceListEntry?
    )

    private val log by logger()

    /**
     * @see [ICoverArtRepository.retrieveCoverArtImage]
     */
    override suspend fun retrieveCoverArtImage(coverRequest: CoverArtRequestDataEntity): String? {

        log.info(this::retrieveCoverArtImage.name) {
            "requested with title=${coverRequest.title} artist=${coverRequest.artist}" +
                    "album=${coverRequest.album}"
        }

        val offlineCover = loadOfflineCover(coverRequest)

        if (offlineCover != null) {
            return offlineCover
        }

        return loadOnlineCover(coverRequest)
    }

    private suspend fun loadOfflineCover(coverRequest: CoverArtRequestDataEntity): String? {
        coverRequest.lifeCycleData = LifeCycleDataEntity(
            lastRequested = timeProvider.secondsSinceEpoch()
        )

        val cachedImageEntity = dataStore.getCoverArt(coverRequest)
        return cachedImageEntity?.uri?.also {
            log.info(this::loadOfflineCover.name) {
                "found cached cover for title=${coverRequest.title} " +
                        "artist=${coverRequest.artist}" +
                        "album=${coverRequest.album}: result uri = $it"
            }
        }
    }

    private suspend fun loadOnlineCover(coverRequest: CoverArtRequestDataEntity): String? {
        val lookupMode = onlineStateProvider.isOnlineLookupAllowed(dataProvider.serviceId)
        if (lookupMode != LookupMode.FULL) {
            log.warn(this::loadOnlineCover.name) {
                "online lookup not fully available (lookupMode=$lookupMode), continue with best-effort lookup"
            }
        }

        val disableReasons = onlineStateProvider.disableReasonsFlow(dataProvider.serviceId).value
        if (disableReasons?.hasDisableReasons == true) {
            log.warn(this::loadOnlineCover.name) {
                "disable reasons present ($disableReasons), continue with best-effort lookup"
            }
        }

        val config = resolveProviderConfig()

        dataProvider.selectNetwork(config.networkInfo)
        dataProvider.setServiceConfig(config.serviceListEntry)

        val cover = dataProvider.getCoverArtImage(coverRequest)

        if (cover.imageData.isNullOrEmpty()) {
            log.warn(this::loadOnlineCover.name) {
                "no online result found for title=${coverRequest.title} " +
                        "artist=${coverRequest.artist} album=${coverRequest.album}"
            }
            return null
        }

        return dataStore.addCoverArtLookup(coverRequest, cover).also {
            log.info(this::retrieveCoverArtImage.name) {
                "found online result for title=${coverRequest.title} " +
                        "artist=${coverRequest.artist} " +
                        "album=${coverRequest.album}: result uri = $it"
            }
        }
    }

    private suspend fun resolveProviderConfig(): ProviderConfig {
        val snapshotNetwork = onlineStateProvider.networkFlow(dataProvider.serviceId).value
        val snapshotService = onlineStateProvider.serviceListFlow(dataProvider.serviceId).value

        if (snapshotNetwork != null && snapshotService != null) {
            return ProviderConfig(snapshotNetwork, snapshotService)
        }

        val waitedOnlineState = withTimeoutOrNull(ONLINE_STATE_WAIT_TIMEOUT_MS) {
            onlineStateProvider.onlineStateFlow(dataProvider.serviceId, onlineOnly = false)
                .first { state ->
                    state?.networkInfo != null && state.serviceListEntry != null
                }
        }

        val resolvedNetwork = waitedOnlineState?.networkInfo
            ?: onlineStateProvider.networkFlow(dataProvider.serviceId).value
            ?: snapshotNetwork
        val resolvedService = waitedOnlineState?.serviceListEntry
            ?: onlineStateProvider.serviceListFlow(dataProvider.serviceId).value
            ?: snapshotService

        if (waitedOnlineState != null) {
            log.info(this::resolveProviderConfig.name) {
                "using refreshed online state after wait"
            }
        } else {
            log.warn(this::resolveProviderConfig.name) {
                "no fully refreshed online state within ${ONLINE_STATE_WAIT_TIMEOUT_MS}ms; " +
                    "continue with best available snapshot"
            }
        }

        return ProviderConfig(resolvedNetwork, resolvedService)
    }
}

