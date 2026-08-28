package technology.cariad.entertainment.metadataservice.data.provider

import technology.cariad.entertainment.metadataservice.common.io.generateFilename
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGracenoteService
import technology.cariad.entertainment.metadataservice.domain.ICoverArtProvider
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CoverArtProvider(
    override val serviceId: ServiceIdentifier,
    private val gnService: IGracenoteService
) : ICoverArtProvider {

    private val log by logger()

    /**
     * @see [ICoverArtProvider.getCoverArtImage]
     */
    override suspend fun getCoverArtImage(coverArtInfo: CoverArtRequestDataEntity): ImageEntity =
        suspendCoroutine { cont ->

            log.info("getCoverArtImage") {
                "artist=${coverArtInfo.artist} album=${coverArtInfo.album} " +
                        "title=${coverArtInfo.title}"
            }

            gnService.doTextLookupForAlbum(
                coverArtInfo.title,
                coverArtInfo.artist,
                coverArtInfo.album
            ) { imageData ->

                val filename = if (imageData != null && imageData.isNotEmpty()) {
                    generateFilename("jpg")
                } else ""

                val imageEntity = ImageEntity(
                    null,
                    filename,
                    ImageEntity.Companion.ImageType.COVER_ART,
                    imageData?.toList()
                )

                log.info("onCoverArtReceived") {
                    "artist=${coverArtInfo.artist} album=${coverArtInfo.album} " +
                            "title=${coverArtInfo.title} size=${imageData?.size}B " +
                            "filename=$filename"
                }

                cont.resume(imageEntity)
            }
        }

    /**
     * @see [IOnlineService.selectNetwork]
     */
    override fun selectNetwork(networkInfo: NetworkInfo?) {
        log.info(this::selectNetwork.name) {
            "set network to: networkInfo=$networkInfo"
        }

        gnService.updateApnConfigEntry(GnServiceRequestNr.COVERART, networkInfo?.interfaceName)
    }
}

