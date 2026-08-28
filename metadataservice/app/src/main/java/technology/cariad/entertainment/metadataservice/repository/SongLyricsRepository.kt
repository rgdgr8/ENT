package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.networkaccess.INetworkAccessProvider
import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsProvider
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsRepository
import technology.cariad.entertainment.metadataservice.domain.LookupMode
import technology.cariad.entertainment.metadataservice.domain.entity.SongLyricsEntity
import technology.cariad.entertainment.metadataservice.domain.value.LyricsType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class SongLyricsRepository(
    private val dataProvider: ISongLyricsProvider,
    private val onlineStateProvider: IOnlineStateProvider,
    private val networkAccessProvider: INetworkAccessProvider
) : ISongLyricsRepository {

    private val log by logger()

    /**
     * @see [ISongLyricsRepository.retrieveSongLyrics]
     */
    override fun retrieveSongLyrics(
        lyricsType: LyricsType,
        familyFriendly: Boolean
    ): Flow<Result<SongLyricsEntity>> {
        return onlineStateProvider.onlineStateFlow(dataProvider.serviceId)
            .filterNotNull()
            .flatMapLatest { onlineState ->
                if (
                    onlineStateProvider.isOnlineLookupAllowed(
                        dataProvider.serviceId
                    ) != LookupMode.FULL || onlineState.countryOfSales == "--"
                ) {
                    log.warn(this@SongLyricsRepository::retrieveSongLyrics.name) {
                        "gracenote online functionality not available: " +
                                "countryOfSales=${onlineState.countryOfSales}"
                    }
                    emptyFlow()
                } else {

                    log.info(this@SongLyricsRepository::retrieveSongLyrics.name) {
                        "retrieving mcc"
                    }

                    val mcc = networkAccessProvider.mccFlow().filterNotNull().first()
                    dataProvider.setMcc(mcc)
                    dataProvider.selectNetwork(onlineState.networkInfo)

                    log.info(this@SongLyricsRepository::retrieveSongLyrics.name) {
                        "forward request to provide using " +
                                "network=${onlineState.networkInfo} mcc=$mcc"
                    }

                    dataProvider.getSongLyrics(lyricsType, familyFriendly).map {
                        val songLyrics = it

                        log.info(this@SongLyricsRepository::retrieveSongLyrics.name) {
                            "update metadata: $songLyrics"
                        }
                        Success(songLyrics)
                    }
                }
            }
    }
}

