package technology.cariad.entertainment.metadataservice.data.provider

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.data.mapper.LyricsTypeMapper
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGracenoteService
import technology.cariad.entertainment.metadataservice.domain.ISongLyricsProvider
import technology.cariad.entertainment.metadataservice.domain.entity.SongLyricsEntity
import technology.cariad.entertainment.metadataservice.domain.value.LyricsType
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SongLyricsProvider(
    override val serviceId: ServiceIdentifier,
    private val gnService: IGracenoteService
) : ISongLyricsProvider {

    private val log by logger()

    /**
     * @see [ISongLyricsProvider.getSongLyrics]
     */
    override fun getSongLyrics(
        lyricsType: LyricsType,
        familyFriendly: Boolean
    ): Flow<SongLyricsEntity> = callbackFlow {
        log.info(this@SongLyricsProvider::getSongLyrics.name) { "register new subscriber" }

        gnService.requestLyrics(
            LyricsTypeMapper().mapValue(lyricsType),
            familyFriendly
        ) { playPosition: Int, lyricsData: String? ->

            log.info(this@SongLyricsProvider::getSongLyrics.name) {
                "received new lyrics: length=${lyricsData?.length}"
            }

            trySend(SongLyricsEntity(lyricsData ?: "", playPosition))
        }

        awaitClose()
    }

    /**
     * @see [ISongLyricsProvider.selectNetwork]
     */
    override fun selectNetwork(networkInfo: NetworkInfo?) {
        log.info(this::selectNetwork.name) {
            "set network to: networkInfo=$networkInfo"
        }

        gnService.updateApnConfigEntry(
            GnServiceRequestNr.LYRICS,
            networkInfo?.interfaceName
        )
    }

    /**
     * @see [ISongLyricsProvider.setMcc]
     */
    override fun setMcc(mcc: Int) {
        log.info(this::selectNetwork.name) {
            "set mcc to: mcc=$mcc"
        }

        gnService.setMcc(mcc)
    }
}

