package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataProvider
import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataRepository
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MusicMetadataRepository(
    private val dataProvider: IMusicMetadataProvider,
    private val onlineStateProvider: IOnlineStateProvider
) : IMusicMetadataRepository {

    private val log by logger()

    override suspend fun getAlbumMetadata(albumId: String): MusicMetadataEntity {
        return onlineStateProvider.onlineStateFlow(dataProvider.serviceId).filterNotNull()
            .map { onlineState ->
                dataProvider.selectNetwork(onlineState.networkInfo)
                dataProvider.setServiceConfig(onlineState.serviceListEntry)

                log.info(this::getAlbumMetadata.name) {
                    "retrieve metadata for albumId=$albumId"
                }

                dataProvider.getAlbumMetadata(albumId).also {
                    log.info(this::getAlbumMetadata.name) {
                        "forward metadata for albumId=$albumId:  result=$it"
                    }
                }
            }.first()
    }

    override suspend fun getArtistMetadata(artistId: String): MusicMetadataEntity {
        return onlineStateProvider.onlineStateFlow(dataProvider.serviceId).filterNotNull()
            .map { onlineState ->
                dataProvider.selectNetwork(onlineState.networkInfo)
                dataProvider.setServiceConfig(onlineState.serviceListEntry)

                log.info(this::getArtistMetadata.name) {
                    "retrieve metadata for artistId=$artistId"
                }

                dataProvider.getArtistMetadata(artistId).also {
                    log.info(this::getAlbumMetadata.name) {
                        "forward metadata for artistId=$artistId:  result=$it"
                    }
                }
            }.first()
    }

    override suspend fun getRecordingMetadata(recordingId: String): MusicMetadataEntity {
        return onlineStateProvider.onlineStateFlow(dataProvider.serviceId).filterNotNull()
            .map { onlineState ->
                dataProvider.selectNetwork(onlineState.networkInfo)
                dataProvider.setServiceConfig(onlineState.serviceListEntry)

                log.info(this::getRecordingMetadata.name) {
                    "retrieve metadata for recordingId=$recordingId"
                }

                dataProvider.getRecordingMetadata(recordingId).also {
                    log.info(this::getAlbumMetadata.name) {
                        "forward metadata for recordingId=$recordingId:  result=$it"
                    }
                }
            }.first()
    }

    override suspend fun getRecordingMetadataByName(
        recordingName: String,
        artistName: String
    ): MusicMetadataEntity {
        return onlineStateProvider.onlineStateFlow(dataProvider.serviceId).filterNotNull()
            .map { onlineState ->
                dataProvider.selectNetwork(onlineState.networkInfo)
                dataProvider.setServiceConfig(onlineState.serviceListEntry)

                log.info(this::getRecordingMetadataByName.name) {
                    "retrieve metadata for recordingName=$recordingName artistName=$artistName"
                }

                dataProvider.getRecordingMetadataByName(recordingName, artistName).also {
                    log.info(this::getAlbumMetadata.name) {
                        "forward metadata for recordingName=$recordingName" +
                                "artistName=$artistName:" +
                                " result=$it"
                    }
                }
            }.first()
    }
}

