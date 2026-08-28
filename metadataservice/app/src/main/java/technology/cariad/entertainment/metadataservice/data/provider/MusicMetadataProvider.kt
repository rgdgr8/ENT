package technology.cariad.entertainment.metadataservice.data.provider

import de.esolutions.magicadapter.stapi.AuthenticatorConstants
import de.esolutions.gmdlib.data.AccountManagerConfig
import de.esolutions.gmdlib.data.EndpointConfig
import de.esolutions.gmdlib.data.GmdErrorType
import de.esolutions.gmdlib.media.GmdClient
import de.esolutions.gmdlib.media.IGracenoteMusicDataCallback
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.data.mapper.MusicMetadataErrorMapper
import technology.cariad.entertainment.metadataservice.domain.IMusicMetadataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val OP_RECORDING_STRING_SEARCH = "RECORDINGS_STRING_SEARCH"
private const val OP_ARTISTS = "ARTISTS"
private const val OP_RECORDINGS = "RECORDINGS"
private const val OP_ALBUMS = "ALBUMS"

class MusicMetadataProvider(
    override val serviceId: ServiceIdentifier,
    private val gmdClient: GmdClient
) : IMusicMetadataProvider {

    private val log by logger()

    override suspend fun getAlbumMetadata(albumId: String): MusicMetadataEntity =
        suspendCoroutine { cont ->

            val callback = object : IGracenoteMusicDataCallback {

                override fun onSuccess(response: String) {

                    log.info(this::onSuccess.name) {
                        "received album metadata for albumId=$albumId: response=$response"
                    }

                    cont.resume(MusicMetadataEntity(response))
                }

                override fun onError(
                    errorType: GmdErrorType,
                    httpStatusCode: Int?,
                    message: String
                ) {
                    val metadataError = MusicMetadataErrorMapper().mapValue(errorType)
                    val metadata = MusicMetadataEntity(metadataError, "$httpStatusCode", message)

                    log.warn(this::onError.name) {
                        "received error for albumId=$albumId: error=$metadataError"
                    }

                    cont.resume(metadata)
                }
            }

            log.info("getAlbumMetadata") {
                "request metadata for albumId=$albumId"
            }

            gmdClient.getAlbumMetadata(albumId, callback)
        }

    override suspend fun getArtistMetadata(artistId: String): MusicMetadataEntity =
        suspendCoroutine { cont ->

            val callback = object : IGracenoteMusicDataCallback {

                override fun onSuccess(response: String) {

                    log.info(this::onSuccess.name) {
                        "received artist metadata for artistId=$artistId: response=$response"
                    }

                    cont.resume(MusicMetadataEntity(response))
                }

                override fun onError(
                    errorType: GmdErrorType,
                    httpStatusCode: Int?,
                    message: String
                ) {
                    val metadataError = MusicMetadataErrorMapper().mapValue(errorType)
                    val metadata = MusicMetadataEntity(metadataError, "$httpStatusCode", message)

                    log.warn(this::onError.name) {
                        "received error for artistId=$artistId: error=$metadataError"
                    }

                    cont.resume(metadata)
                }
            }

            log.info("getArtistMetadata") {
                "request metadata for artistId=$artistId"
            }

            gmdClient.getArtistMetadata(artistId, callback)
        }

    override suspend fun getRecordingMetadata(recordingId: String): MusicMetadataEntity =
        suspendCoroutine { cont ->

            val callback = object : IGracenoteMusicDataCallback {

                override fun onSuccess(response: String) {

                    log.info(this::onSuccess.name) {
                        "received recording metadata for recordingId=$recordingId:" +
                                " response=$response"
                    }

                    cont.resume(MusicMetadataEntity(response))
                }

                override fun onError(
                    errorType: GmdErrorType,
                    httpStatusCode: Int?,
                    message: String
                ) {
                    val metadataError = MusicMetadataErrorMapper().mapValue(errorType)
                    val metadata = MusicMetadataEntity(metadataError, "$httpStatusCode", message)

                    log.warn(this::onError.name) {
                        "received error for recordingId=$recordingId: error=$metadataError"
                    }

                    cont.resume(metadata)
                }
            }

            log.info("getRecordingMetadata") {
                "request metadata for recordingId=$recordingId"
            }

            gmdClient.getRecordingMetadata(recordingId, callback)
        }

    override suspend fun getRecordingMetadataByName(
        recordingName: String,
        artistName: String
    ): MusicMetadataEntity = suspendCoroutine { cont ->

        val callback = object : IGracenoteMusicDataCallback {

            override fun onSuccess(response: String) {

                log.info(this::onSuccess.name) {
                    "received recording metadata for " +
                            "recordingName=$recordingName artistName=$artistName: " +
                            "response=$response"
                }

                cont.resume(MusicMetadataEntity(response))
            }

            override fun onError(errorType: GmdErrorType, httpStatusCode: Int?, message: String) {
                val metadataError = MusicMetadataErrorMapper().mapValue(errorType)
                val metadata = MusicMetadataEntity(metadataError, "$httpStatusCode", message)

                log.warn(this::onError.name) {
                    "received error for recordingName=$recordingName artistName=$artistName:" +
                            " error=$metadataError"
                }

                cont.resume(metadata)
            }
        }

        log.info("getRecordingMetadataByName") {
            "request metadata for recordingName=$recordingName artistName=$artistName"
        }

        gmdClient.getRecordingMetadata(recordingName, artistName, callback)
    }

    /**
     * @see [IRadioStationDataProvider.selectNetwork]
     */
    override fun selectNetwork(networkInfo: NetworkInfo?) {
        log.info(this::selectNetwork.name) {
            "set network to: networkInfo=$networkInfo"
        }

        gmdClient.setNetwork(networkInfo?.network)
    }

    /**
     * @see [IRadioStationDataProvider.setServiceConfig]
     */
    override fun setServiceConfig(serviceListEntry: ServiceListEntry?) {
        log.info(this::setServiceConfig.name) {
            "service list changed: serviceListEntry=$serviceListEntry"
        }

        val accountManagerConfig = AccountManagerConfig(
            serviceId = serviceId.serviceName,
            accountType = AuthenticatorConstants.CORESERVICE_ACCOUNT_TYPE,
            accountName = AuthenticatorConstants.CORESERVICE_ACCOUNT_NAME,
            optionServiceId = AuthenticatorConstants.BUNDLE_OPTIONS_TRANSACTIONTYPEID,
            optionOperationId = AuthenticatorConstants.BUNDLE_OPTIONS_OPERATIONTYPEID
        )

        val endpointConfigRecordingStringSearch = EndpointConfig(
            serviceListEntry?.httpOperations?.get(OP_RECORDING_STRING_SEARCH)?.url ?: "",
            OP_RECORDING_STRING_SEARCH
        )
        val endpointConfigArtists = EndpointConfig(
            serviceListEntry?.httpOperations?.get(OP_ARTISTS)?.url ?: "",
            OP_ARTISTS
        )
        val endpointConfigRecording = EndpointConfig(
            serviceListEntry?.httpOperations?.get(OP_RECORDINGS)?.url ?: "",
            OP_RECORDINGS
        )
        val endpointConfigAlbums = EndpointConfig(
            serviceListEntry?.httpOperations?.get(OP_ALBUMS)?.url ?: "",
            OP_ALBUMS
        )

        log.info(this::setServiceConfig.name) {
            "set config to: " +
                    "accountManagerConfig=$accountManagerConfig " +
                    "endpointConfigRecordingStringSearch=$endpointConfigRecordingStringSearch" +
                    "endpointConfigArtists=$endpointConfigArtists" +
                    "endpointConfigRecording=$endpointConfigRecording" +
                    "endpointConfigAlbums=$endpointConfigAlbums"
        }

        gmdClient.setConfig(
            accountManagerConfig,
            endpointConfigArtists,
            endpointConfigAlbums,
            endpointConfigRecording,
            endpointConfigRecordingStringSearch
        )
    }
}

