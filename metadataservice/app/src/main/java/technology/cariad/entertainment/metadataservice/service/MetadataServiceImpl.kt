package technology.cariad.entertainment.metadataservice.service

import android.content.Context
import android.os.Binder
import android.os.DeadObjectException
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.os.TransactionTooLargeException
import technology.cariad.entertainment.metadataservice.stubs.fw.tracing.ITraceCallback
import technology.cariad.entertainment.metadataservice.stubs.fw.tracing.TraceClient
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.permission.IPermissionManager
import technology.cariad.entertainment.metadataservice.common.permission.Permission
import technology.cariad.entertainment.metadataservice.common.util.Error
import technology.cariad.entertainment.metadataservice.common.util.ServiceDisabledException
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.IMutableOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.service.mapper.BearerAsiaMapper
import technology.cariad.entertainment.metadataservice.service.mapper.BearerMapper
import technology.cariad.entertainment.metadataservice.service.mapper.CoverArtRequestDataMapper
import technology.cariad.entertainment.metadataservice.service.mapper.LyricsTypeMapper
import technology.cariad.entertainment.metadataservice.service.mapper.MusicMetadataMapper
import technology.cariad.entertainment.metadataservice.service.mapper.PermissionMapper
import technology.cariad.entertainment.metadataservice.service.mapper.SongInfoMapper
import technology.cariad.entertainment.metadataservice.service.mapper.SongLyricsMapper
import technology.cariad.entertainment.metadataservice.service.mapper.StationMapper
import technology.cariad.entertainment.metadataserviceclient.api.BooleanSingleCallback
import technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidl
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceManagement
import technology.cariad.entertainment.metadataserviceclient.api.MusicMetadataSingleCallback
import technology.cariad.entertainment.metadataserviceclient.api.PermissionData
import technology.cariad.entertainment.metadataserviceclient.api.PermissionDataSingleCallback
import technology.cariad.entertainment.metadataserviceclient.api.ProviderIdentifier
import technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfoCallback
import technology.cariad.entertainment.metadataserviceclient.api.SongLyricsCallback
import technology.cariad.entertainment.metadataserviceclient.api.StationAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationAsiaSingleCallback
import technology.cariad.entertainment.metadataserviceclient.api.StationBearer
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationCallback
import technology.cariad.entertainment.metadataserviceclient.api.StationLogo
import technology.cariad.entertainment.metadataserviceclient.api.StringCallback
import technology.cariad.entertainment.metadataserviceclient.util.TypedBearer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject

class MetadataServiceImpl : IMetadataServiceAidl.Stub(), ITraceCallback {

    private val log by logger()

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var onlineStateProvider: IMutableOnlineStateProvider

    @Inject
    lateinit var configurationProvider: IConfigurationProvider

    @Inject
    lateinit var permissionManager: IPermissionManager

    private var isInjected = false
    private val injectLock = ReentrantLock()

    private val management = MetadataServiceManagementImpl()

    override fun management(): IMetadataServiceManagement {
        return management
    }

    init {
        val id = TraceClient.registerTraceCallback("gnsdk_triggerOnlineJob", this)
        log.info("init") { "TLS Test: registered callback with id $id" }
    }

    /**
     * Enforces the client contract permission for every AIDL call. The caller must hold
     * either the legacy or the current USE_SERVICE permission (see [ClientPermission]).
     *
     * Binding is intentionally unrestricted in the manifest so that both legacy and newer
     * clients can connect; authorization happens here where the Binder calling identity
     * reflects the client process. Only application-defined transactions are checked so
     * that framework transactions (interface descriptor, dump, ...) keep working.
     */
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (code in IBinder.FIRST_CALL_TRANSACTION..IBinder.LAST_CALL_TRANSACTION &&
            !ClientPermission.hasUseServicePermission(MetadataServiceApp.app)
        ) {
            val uid = Binder.getCallingUid()
            log.warn("onTransact") {
                "denied AIDL call code=$code from uid=$uid: missing USE_SERVICE permission"
            }
            throw SecurityException("caller does not hold USE_SERVICE permission")
        }
        return super.onTransact(code, data, reply, flags)
    }

    /**
     * @see [IMetadataServiceAidl.changeOnlineLookupStateSubscribe]
     */
    override fun changeOnlineLookupStateSubscribe(
        clientId: Int,
        requestId: Int,
        enabled: Boolean,
        callback: BooleanSingleCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            log.error(this::changeOnlineLookupStateSubscribe.name) {
                "cannot process request - service not initialized"
            }
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::changeOnlineLookupStateSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::changeOnlineLookupStateSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            return
        }

        val newState = onlineStateProvider.updateOnlineLookupState(enabled)

        log.info(this::changeOnlineLookupStateSubscribe.name) {
            "[$clientId,$requestId] requested enabled=$enabled, new state=$newState"
        }
        catchRemoteError(clientId, requestId, uid) {
            callback.onSuccess(newState)
        }
    }

    /**
     * @see [IMetadataServiceAidl.changeOnlineLookupStateDispose]
     */
    override fun changeOnlineLookupStateDispose(clientId: Int, requestId: Int) {
        log.info(this::changeOnlineLookupStateDispose.name) { "[$clientId){$requestId] ignored" }
    }

    /**
     * @see [IMetadataServiceAidl.observeRadioStationDataSubscribe]
     */
    override fun observeRadioStationDataSubscribe(
        clientId: Int,
        requestId: Int,
        bearer: StationBearer,
        cachedETag: String,
        callback: StationCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::observeRadioStationDataSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::observeRadioStationDataSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeRadioStationDataSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            IRCLogger.appGenericError(
                "client not registered",
                "invalid request from unregistered client (uid=$uid)"
            )
            return
        }

        val typedBearer = TypedBearer.fromRawBearer(bearer) ?: run {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("invalid bearer")
            }
            return
        }

        val bearerEntity = BearerMapper().mapValue(typedBearer)

        log.info(this::observeRadioStationDataSubscribe.name) {
            "clientId=$clientId requestId=$requestId bearer=${bearerEntity.toBearerString()} " +
                    "lastModified=$cachedETag"
        }

        val callerUid = Binder.getCallingUid()
        val grantLogoReadAccess = { logo: ImageEntity? ->
            logo?.uri?.let {
                AccessManager.grantUriPermission(callerUid, it)
            }
        }

        val useCase =
            getDomain(configurationProvider.getOperationMode()).retrieveRadioStationDataFactory()
                .create()

        val job = useCase.logoByBearer(bearerEntity, ETagsEntity.fromString(cachedETag))
            .onEach {
                grantLogoReadAccess(it.logo)
                grantLogoReadAccess(it.logoSmall)
            }
            .map { StationMapper().mapValue(it) }
            .onEach { station ->
                catchRemoteError(clientId, requestId, uid) {
                    callback.onNext(station)
                }
            }
            .catch { e ->
                log.error(this@MetadataServiceImpl::observeRadioStationDataSubscribe.name) {
                    "[$clientId,$requestId] exception caught: ${e.message}"
                }
                try {
                    catchRemoteError(clientId, requestId, uid) {
                        callback.onError(e.message)
                    }
                } catch (e: RemoteException) {
                    log.error(this@MetadataServiceImpl::observeRadioStationDataSubscribe.name) {
                        "[$clientId,$requestId] client died: cannot forward error. ${e.message}"
                    }
                }
            }
            .onCompletion {
                if (management.removeRequest(clientId, requestId)) {
                    log.info(this@MetadataServiceImpl::observeRadioStationDataSubscribe.name) {
                        "[$clientId,$requestId] successfully completed - notify client"
                    }

                    catchRemoteError(clientId, requestId, uid) {
                        callback.onComplete()
                    }
                } else {
                    log.info(this@MetadataServiceImpl::observeRadioStationDataSubscribe.name) {
                        "[$clientId,$requestId] request canceled - don't notify client"
                    }
                }
            }
            .launchIn(GlobalScope) //TODO: implement proper scoping

        management.addRequest(clientId, requestId, job)
    }

    /**
     * @see [IMetadataServiceAidl.observeRadioStationDataDispose]
     */
    override fun observeRadioStationDataDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeRadioStationDataDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::observeRadioStationDataDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    /**
     * @see [IMetadataServiceAidl.observeSongRecognitionSubscribe]
     */
    override fun observeSongRecognitionSubscribe(
        clientId: Int,
        requestId: Int,
        callback: RecognizedSongInfoCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::observeSongRecognitionSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "cannot process request",
                "service not initialized"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeSongRecognitionSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            try {
                catchRemoteError(clientId, requestId, uid) {
                    callback.onError("client not registered")
                }
            } catch (e: RemoteException) {
                log.error(this@MetadataServiceImpl::observeSongRecognitionSubscribe.name) {
                    "[$clientId,$requestId] client died: cannot forward error. ${e.message}."
                }
                IRCLogger.appGenericError(
                    "[$clientId,$requestId] client died",
                    "cannot forward error. ${e.message}"
                )
            }

            return
        }

        log.info(this::observeSongRecognitionSubscribe.name) { "[$clientId){$requestId]" }

        val useCase = getDomain().retrieveSongInfoFactory().create()

        val job = useCase.songInfoByFingerprinting()
            .onEach {
                when (it) {
                    is Success -> {
                        it.result.coverartUri?.apply {
                            AccessManager.grantUriPermission(
                                uid,
                                this
                            )
                        }
                        val songInfo = SongInfoMapper().mapValue(it.result)

                        if (management.requestRunning(clientId, requestId)) {
                            catchRemoteError(clientId, requestId, uid) {
                                callback.onNext(songInfo)
                            }
                        }
                    }
                    is Error -> {
                        log.error(this@MetadataServiceImpl::observeSongRecognitionSubscribe.name) {
                            "[$clientId,$requestId] exception caught: ${it.throwable.message}"
                        }
                        catchRemoteError(clientId, requestId, uid) {
                            callback.onError(it.throwable.message)
                        }
                    }
                }
            }
            .onCompletion {
                if (management.removeRequest(clientId, requestId)) {
                    log.info(this@MetadataServiceImpl::observeSongRecognitionSubscribe.name) {
                        "[$clientId,$requestId] successfully completed - notify client"
                    }

                    try {
                        catchRemoteError(clientId, requestId, uid) {
                            callback.onComplete()
                        }
                    } catch (e: RemoteException) {
                        log.error(
                            this@MetadataServiceImpl::observeSongRecognitionSubscribe.name
                        ) {
                            "[$clientId,$requestId] client died: cannot forward request " +
                                    "completed. ${e.message}."
                        }
                        IRCLogger.appGenericError(
                            "[$clientId,$requestId] client died",
                            "cannot forward request completed. ${e.message}."
                        )
                    }
                } else {
                    log.info(this@MetadataServiceImpl::observeSongRecognitionSubscribe.name) {
                        "[$clientId,$requestId] request canceled - don't notify client"
                    }
                }
            }
            .launchIn(GlobalScope) //TODO: implement proper scoping

        management.addRequest(clientId, requestId, job)
    }

    /**
     * @see [IMetadataServiceAidl.observeSongRecognitionDispose]
     */
    override fun observeSongRecognitionDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeSongRecognitionDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::observeSongRecognitionDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    /**
     * @see [IMetadataServiceAidl.getCoverArtSubscribe]
     */
    override fun getCoverArtSubscribe(
        clientId: Int,
        requestId: Int,
        requestData: CoverArtRequestData,
        callback: StringCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::getCoverArtSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::getCoverArtSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getCoverArtSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            return
        }

        log.info(this::getCoverArtSubscribe.name) {
            "[$clientId,$requestId] album=${requestData.album} artist=${requestData.artist} " +
                    "title=${requestData.title}"
        }

        val useCase = getDomain().textBasedCoverArtLookupFactory().create()
        val callerUid = Binder.getCallingUid()

        //TODO: scoping
        val job = GlobalScope.launch {
            val dataEntity = CoverArtRequestDataMapper().mapValue(requestData)
            try {
                val result = useCase.coverArtImageByInfo(dataEntity)

                if (management.removeRequest(clientId, requestId)) {
                    log.info(this@MetadataServiceImpl::getCoverArtSubscribe.name) {
                        "[$clientId,$requestId] successfully completed - notify client"
                    }

                    result?.let {
                        AccessManager.grantUriPermission(callerUid, it)
                        catchRemoteError(clientId, requestId, uid) {
                            callback.onNext(it)
                        }
                    }

                    catchRemoteError(clientId, requestId, uid) {
                        callback.onComplete()
                    }
                } else {
                    log.info(this@MetadataServiceImpl::getCoverArtSubscribe.name) {
                        "[$clientId,$requestId] request canceled - don't notify client"
                    }
                }
            } catch (e: ServiceDisabledException) {
                if (management.removeRequest(clientId, requestId)) {
                    log.info(this@MetadataServiceImpl::getCoverArtSubscribe.name) {
                        "[$clientId,$requestId] failed with exception: msg=${e.message} " +
                                "- notify client"
                    }
                    catchRemoteError(clientId, requestId, uid) {
                        callback.onError(e.message)
                    }
                } else {
                    log.info(this@MetadataServiceImpl::getCoverArtSubscribe.name) {
                        "[$clientId,$requestId] request canceled - don't notify client"
                    }
                }
            }
        }

        management.addRequest(clientId, requestId, job)
    }

    /**
     * @see [IMetadataServiceAidl.getCoverArtDispose]
     */
    override fun getCoverArtDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getCoverArtDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::getCoverArtDispose.name) { "[$clientId){$requestId] cancel request" }

        management.cancelRequest(clientId, requestId)
    }

    /**
     * @see [IMetadataServiceAidl.getPermissionDataSubscribe]
     */
    override fun getPermissionDataSubscribe(
        clientId: Int,
        requestId: Int,
        callback: PermissionDataSingleCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::getPermissionDataSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::getPermissionDataSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getPermissionDataSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            return
        }

        val missingPermissions = permissionManager.getDeniedPermissions()
        val missingPermissionFlags = PermissionMapper().mapList(missingPermissions).distinct()

        log.info(this::getPermissionDataSubscribe.name) {
            "[$clientId,$requestId] missing permissions: ${missingPermissionFlags.joinToString()}"
        }
        catchRemoteError(clientId, requestId, uid) {
            callback.onSuccess(PermissionData(missingPermissionFlags))
        }
    }

    /**
     * @see [IMetadataServiceAidl.registerClient]
     */
    override fun getPermissionDataDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getPermissionDataDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::getPermissionDataDispose.name) { "[$clientId){$requestId] cancel request" }

        management.cancelRequest(clientId, requestId)
    }

    /**
     * @see [IMetadataServiceAidl.observeRadioStationDataAsiaSubscribe]
     */
    override fun observeRadioStationDataAsiaSubscribe(
        clientId: Int,
        requestId: Int,
        bearer: StationBearerAsia,
        callback: StationAsiaSingleCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::observeRadioStationDataAsiaSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::observeRadioStationDataAsiaSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeRadioStationDataAsiaSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            return
        }

        log.info(this::observeRadioStationDataAsiaSubscribe.name) {
            "clientId=$clientId requestId=$requestId bearer=${bearer}"
        }

        if (!permissionManager.hasPermission(Permission.ACCESS_BACKGROUND_LOCATION) ||
            !permissionManager.hasPermission(Permission.ACCESS_FINE_LOCATION)
        ) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("missing permissions")
            }
            return
        }

        //TODO: scoping
        val job = GlobalScope.launch {
            val useCase = getDomain().retrieveRadioStationAsiaFactory().create()
            val bearerEntity = BearerAsiaMapper().mapValue(bearer)
            val stationData = useCase.dataByBearer(bearerEntity)

            if (management.removeRequest(clientId, requestId)) {
                log.info(this@MetadataServiceImpl::observeRadioStationDataAsiaSubscribe.name) {
                    "[$clientId,$requestId] successfully completed - notify client"
                }

                stationData.logo?.uri?.let {
                    AccessManager.grantUriPermission(uid, it)
                }

                catchRemoteError(clientId, requestId, uid) {
                    callback.onSuccess(
                        StationAsia(
                            stationData.name,
                            StationLogo(stationData.logo?.uri ?: ""),
                            ProviderIdentifier(
                                ProviderIdentifier.ORIGIN_TYPE_GRACENOTE,
                                stationData.providerIdentifier?.id ?: ""
                            )
                        )
                    )
                }
            }
        }

        management.addRequest(clientId, requestId, job)
    }

    /**
     * @see [IMetadataServiceAidl.observeRadioStationDataAsiaDispose]
     */
    override fun observeRadioStationDataAsiaDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeRadioStationDataAsiaDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::observeRadioStationDataAsiaDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    /**
     * @see [IMetadataServiceAidl.observeSongLyricsSubscribe]]
     */
    override fun observeSongLyricsSubscribe(
        clientId: Int,
        requestId: Int,
        lyricsType: Int,
        familyFriendly: Boolean,
        callback: SongLyricsCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::observeSongLyricsSubscribe.name) {
                "cannot process request - service not initialized"
            }
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeSongLyricsSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            return
        }

        val useCase = getDomain().retrieveSongLyricsFactory().create()

        val job = useCase.songLyrics(LyricsTypeMapper().mapValue(lyricsType), familyFriendly)
            .onEach {
                when (it) {
                    is Success -> {
                        val songInfo = SongLyricsMapper().mapValue(it.result)

                        if (management.requestRunning(clientId, requestId)) {
                            catchRemoteError(clientId, requestId, uid) {
                                callback.onNext(songInfo)
                            }
                        }
                    }
                    is Error -> {
                        log.error(this@MetadataServiceImpl::observeSongLyricsSubscribe.name) {
                            "[$clientId,$requestId] exception caught: ${it.throwable.message}"
                        }
                        catchRemoteError(clientId, requestId, uid) {
                            callback.onError(it.throwable.message)
                        }
                    }
                }
            }
            .onCompletion {
                if (management.removeRequest(clientId, requestId)) {
                    log.info(this@MetadataServiceImpl::observeSongLyricsSubscribe.name) {
                        "[$clientId,$requestId] successfully completed - notify client"
                    }

                    catchRemoteError(clientId, requestId, uid) {
                        callback.onComplete()
                    }

                } else {
                    log.info(this@MetadataServiceImpl::observeSongLyricsSubscribe.name) {
                        "[$clientId,$requestId] request canceled - don't notify client"
                    }
                }
            }
            .launchIn(GlobalScope) //TODO: implement proper scoping

        management.addRequest(clientId, requestId, job)
    }

    /**
     * @see [IMetadataServiceAidl.observeSongLyricsDispose]]
     */
    override fun observeSongLyricsDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::observeSongLyricsDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::observeSongLyricsDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    override fun getArtistMetadataSubscribe(
        clientId: Int,
        requestId: Int,
        artistId: String,
        callback: MusicMetadataSingleCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::getArtistMetadataSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::getArtistMetadataSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getArtistMetadataSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            IRCLogger.appGenericError(
                "client not registered",
                "invalid request from unregistered client (uid=$uid)"
            )
            return
        }

        log.info(this::getArtistMetadataSubscribe.name) {
            "clientId=$clientId requestId=$requestId artistId=$artistId"
        }

        //TODO: scoping
        val job = GlobalScope.launch {
            val useCase = getDomain().retrieveArtistMetadataFactory().create()
            val metadataEntity = useCase.retrieveArtistMetadata(artistId)
            val metadata = MusicMetadataMapper().mapValue(metadataEntity)

            if (management.removeRequest(clientId, requestId)) {
                log.info(this@MetadataServiceImpl::getArtistMetadataSubscribe.name) {
                    "[$clientId,$requestId] successfully completed - notify client"
                }

                catchRemoteError(clientId, requestId, uid) {
                    callback.onSuccess(metadata)
                }
            }
        }

        management.addRequest(clientId, requestId, job)
    }

    override fun getArtistMetadataDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getArtistMetadataDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::getArtistMetadataDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    override fun getAlbumMetadataSubscribe(
        clientId: Int,
        requestId: Int,
        albumId: String,
        callback: MusicMetadataSingleCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::getAlbumMetadataSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::getArtistMetadataSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getAlbumMetadataSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            IRCLogger.appGenericError(
                "client not registered",
                "invalid request from unregistered client (uid=$uid)"
            )
            return
        }

        log.info(this::getAlbumMetadataSubscribe.name) {
            "clientId=$clientId requestId=$requestId albumId=$albumId"
        }

        //TODO: scoping
        val job = GlobalScope.launch {
            val useCase = getDomain().retrieveAlbumMetadataFactory().create()
            val metadataEntity = useCase.retrieveAlbumMetadata(albumId)
            val metadata = MusicMetadataMapper().mapValue(metadataEntity)

            if (management.removeRequest(clientId, requestId)) {
                log.info(this@MetadataServiceImpl::getAlbumMetadataSubscribe.name) {
                    "[$clientId,$requestId] successfully completed - notify client"
                }

                catchRemoteError(clientId, requestId, uid) {
                    callback.onSuccess(metadata)
                }
            }
        }

        management.addRequest(clientId, requestId, job)
    }

    override fun getAlbumMetadataDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getAlbumMetadataDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::getAlbumMetadataDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    override fun getRecordingMetadataSubscribe(
        clientId: Int,
        requestId: Int,
        recordingId: String,
        callback: MusicMetadataSingleCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::getRecordingMetadataSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::getRecordingMetadataSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getRecordingMetadataSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            IRCLogger.appGenericError(
                "client not registered",
                "invalid request from unregistered client (uid=$uid)"
            )
            return
        }

        log.info(this::getRecordingMetadataSubscribe.name) {
            "clientId=$clientId requestId=$requestId recordingId=$recordingId"
        }

        //TODO: scoping
        val job = GlobalScope.launch {
            val useCase = getDomain().retrieveRecordingMetadataByIdFactory().create()
            val metadataEntity = useCase.retrieveRecordingMetadata(recordingId)
            val metadata = MusicMetadataMapper().mapValue(metadataEntity)

            if (management.removeRequest(clientId, requestId)) {
                log.info(this@MetadataServiceImpl::getRecordingMetadataSubscribe.name) {
                    "[$clientId,$requestId] successfully completed - notify client"
                }

                catchRemoteError(clientId, requestId, uid) {
                    callback.onSuccess(metadata)
                }
            }
        }

        management.addRequest(clientId, requestId, job)
    }

    override fun getRecordingMetadataDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getRecordingMetadataDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::getRecordingMetadataDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    override fun getRecordingMetadataByNameSubscribe(
        clientId: Int,
        requestId: Int,
        recordingName: String,
        artistName: String,
        callback: MusicMetadataSingleCallback
    ) {
        val uid = Binder.getCallingUid()

        if (!isInitialized()) {
            catchRemoteError(clientId, requestId, uid) {
                callback.onError("service initialization failed")
            }
            log.error(this::getRecordingMetadataByNameSubscribe.name) {
                "cannot process request - service not initialized"
            }
            IRCLogger.appGenericError(
                "service initialization failed",
                "cannot process request: ${this::getRecordingMetadataByNameSubscribe.name}"
            )
            return
        }

        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getRecordingMetadataByNameSubscribe.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }

            catchRemoteError(clientId, requestId, uid) {
                callback.onError("client not registered")
            }
            IRCLogger.appGenericError(
                "client not registered",
                "invalid request from unregistered client (uid=$uid)"
            )
            return
        }

        log.info(this::getRecordingMetadataByNameSubscribe.name) {
            "clientId=$clientId requestId=$requestId " +
                    "recordingName=$recordingName artistName=$artistName"
        }

        //TODO: scoping
        val job = GlobalScope.launch {
            val useCase = getDomain().retrieveRecordingMetadataByNameFactory().create()
            val metadataEntity = useCase.retrieveRecordingMetadata(recordingName, artistName)
            val metadata = MusicMetadataMapper().mapValue(metadataEntity)

            if (management.removeRequest(clientId, requestId)) {
                log.info(this@MetadataServiceImpl::getRecordingMetadataByNameSubscribe.name) {
                    "[$clientId,$requestId] successfully completed - notify client"
                }

                catchRemoteError(clientId, requestId, uid) {
                    callback.onSuccess(metadata)
                }
            }
        }

        management.addRequest(clientId, requestId, job)
    }

    override fun getRecordingMetadataByNameDispose(clientId: Int, requestId: Int) {
        val uid = Binder.getCallingUid()
        if (!management.isRegistered(clientId, uid)) {
            log.warn(this::getRecordingMetadataByNameDispose.name) {
                "[$clientId,$requestId] invalid request from unregistered client (uid=$uid)"
            }
            return
        }

        log.info(this::getRecordingMetadataByNameDispose.name) {
            "[$clientId){$requestId] cancel request"
        }

        management.cancelRequest(clientId, requestId)
    }

    internal fun cancelRequests() {
        management.cancelRequests()
    }

    private fun isInitialized(): Boolean {
        if (isInjected) return true

        synchronized(injectLock) {
            if (isInjected) return true
            if (MetadataServiceApp.isInitialized) {
                MetadataServiceApp.appComponent.inject(this)
                isInjected = true
                return true
            }
        }
        return false
    }

    private fun getDomain(mode: String = IConfigurationProvider.OPERATION_MODE_LIVE) =
        MetadataServiceApp.domainComponent[mode]
            ?: throw AssertionError("invalid configuration of mode=$mode")

    private fun catchRemoteError(
        clientId: Int,
        requestId: Int,
        userId: Int,
        remoteCall: () -> Unit
    ) {
        try {
            remoteCall()
        } catch (exception: DeadObjectException) {
            log.error(this::catchRemoteError.name) {
                "failed to send response to client" +
                        "Id=$clientId requestId=$requestId id=$clientId (uid=$userId): " +
                        "client is dead (${exception.message})"
            }
            IRCLogger.appGenericError(
                "failed to send response to client",
                "Id=$clientId requestId=$requestId id=$clientId (uid=$userId): " +
                        "client is dead (${exception.message})"
            )
            management.removeClient(clientId, userId)
        } catch (exception: TransactionTooLargeException) {
            log.error(this::catchRemoteError.name) {
                "failed to send response to client " +
                        "id=$clientId requestId=$requestId id=$clientId (uid=$userId): " +
                        "transaction size limited exceeded (${exception.message})"
            }
        }
    }

    override fun executeTraceCallback(cbid: Int, payload: ByteArray?) {
        log.info(this::executeTraceCallback.name) {
            "TLS Test: received traceclient callback"
        }

        if (!isInitialized()) {
            log.error(this::executeTraceCallback.name) {
                "TLS Test: cannot process trace callback - service not initialized"
            }
            return
        }

        GlobalScope.launch {
            try {
                val useCase = getDomain().textBasedCoverArtLookupFactory().create()

                // intentionally use invalid metadata to avoid caching a cover in case of successful request
                val result = useCase.coverArtImageByInfo(CoverArtRequestDataMapper()
                    .mapValue(CoverArtRequestData("unknown_artist", "unknown_title", "unknown_album")))

                log.info(this@MetadataServiceImpl::executeTraceCallback.name) {
                    "TLS Test: callback returned with result: $result"
                }
            } catch (e: ServiceDisabledException) {
                log.info(this@MetadataServiceImpl::executeTraceCallback.name) {
                    "TLS Test: callback failed with exception: msg=${e.message} "
                }
            }
        }
    }
}

