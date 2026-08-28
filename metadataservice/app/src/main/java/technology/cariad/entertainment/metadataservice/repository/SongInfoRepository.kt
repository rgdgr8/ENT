package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.ISongInfoProvider
import technology.cariad.entertainment.metadataservice.domain.ISongInfoRepository
import technology.cariad.entertainment.metadataservice.domain.LookupMode
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn

class SongInfoRepository(
    private val dataProvider: ISongInfoProvider,
    private val imageStore: IImageDataStore,
    private val onlineStateProvider: IOnlineStateProvider
) : ISongInfoRepository {

    private val log by logger()

    private val scope = CoroutineScope(Dispatchers.Default)
    private val sharedSongInfoFlow = songInfoFlow().shareIn(
        scope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1
    )

    /**
     * @see [ISongInfoRepository.retrieveSongInfo]
     */
    override fun retrieveSongInfo(replay: Boolean): Flow<Result<SongInfoEntity>> {
        return if (replay) {
            sharedSongInfoFlow
        } else if (sharedSongInfoFlow.replayCache.isNotEmpty()) {
            sharedSongInfoFlow.drop(1)
        } else sharedSongInfoFlow
    }

    private fun songInfoFlow(): Flow<Result<SongInfoEntity>> =
        onlineStateProvider.onlineStateFlow(dataProvider.serviceId)
            .conflate()
            .map { onlineState ->
                val isOnlineAvailable = onlineState?.networkInfo != null &&
                    onlineStateProvider.isOnlineLookupAllowed(dataProvider.serviceId) == LookupMode.FULL &&
                    onlineState.countryOfSales != "--"

                if (!isOnlineAvailable) {
                    log.warn(this@SongInfoRepository::retrieveSongInfo.name) {
                        "gracenote online functionality not available: " +
                            "networkInfo= ${onlineState?.networkInfo} " +
                            "countryOfSales=${onlineState?.countryOfSales}"
                    }
                }

                // Keep song recognition running even if online state is not fully available.
                onlineState?.networkInfo.takeIf { isOnlineAvailable }
            }
            .distinctUntilChangedBy { networkInfo ->
                networkInfo?.let { "${it.network}:${it.apn}:${it.interfaceName}" } ?: "OFFLINE"
            }
            .onEach { networkInfo ->
                log.info(this@SongInfoRepository::retrieveSongInfo.name) {
                    "forward request to provider using network=$networkInfo"
                }
                dataProvider.selectNetwork(networkInfo)
            }
            .flatMapLatest {
                dataProvider.getSongInfo().map {
                    val songInfo = it.first
                    val image = it.second

                    songInfo.coverartUri = createImageUri(image)

                    log.info(this@SongInfoRepository::retrieveSongInfo.name) {
                        "update metadata: $songInfo"
                    }
                    Success(songInfo)
                }
            }
            .distinctUntilChanged()

    private fun createImageUri(image: ImageEntity): String? {
        if (image.imageData.isNullOrEmpty()) {
            return null
        }

        val uri = imageStore.loadImageUri(image.fileName)
        return if (uri.isNullOrEmpty()) {
            imageStore.saveImage(image.imageData!!.toByteArray(), image.fileName) ?: "".also {
                log.info(this::createImageUri.name) {
                    "stored new image: name=${image.fileName} uri=${it}"
                }
            }
        } else {
            uri.also {
                log.info(this::createImageUri.name) {
                    "retrieved image: name=${image.fileName} uri=${it}"
                }
            }
        }
    }
}

