package technology.cariad.entertainment.metadataservice.data.provider.gracenote

import de.esolutions.gracenoteservice.gntest.FrameworkConfigUnpacker
import de.esolutions.gracenoteservice.gntest.GraceNoteServiceJNI
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.MediaCodingValues
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.data.provider.GnServiceRequestNr
import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder.RecordingConfig
import java.util.concurrent.ConcurrentHashMap

class GracenoteService(
    private val dataDir: String,
    private val locale: String,
    private val recordingConfig: RecordingConfig
) : IGracenoteServiceCallback, IGracenoteService {

    private val log by logger()

    private val service = GraceNoteServiceJNI(this)

    private val songRecognitionCallbacks = mutableSetOf<ISongRecognitionCallback>()
    private val radioStationCallbacks = ConcurrentHashMap<RadioStationInfo, IRadioStationCallback>()

    init {
        val gnSdkVersion = getGnSdkVersionInfo()
        initializeGnService()

        log.info("init") {
            "GNSDK for auto version=$gnSdkVersion data folder=$dataDir locale=$locale"
        }
    }

    /**
     * @param callback Song recognition callback to be registered
     * @see [IGracenoteService.registerCallback]
     */
    override fun registerCallback(callback: ISongRecognitionCallback) {
        songRecognitionCallbacks.add(callback)
    }

    /**
     * @param callback Song recognition callback to be unregistered
     * @see [IGracenoteService.unregisterCallback]
     */
    override fun unregisterCallback(callback: ISongRecognitionCallback) {
        songRecognitionCallbacks.remove(callback)
    }

    /**
     * @see [IGracenoteService.updateApnConfigEntry]
     */
    override fun updateApnConfigEntry(requestNr: GnServiceRequestNr, networkInterface: String?) {
        service.updateApnConfigEntry(requestNr, networkInterface)
    }

    /**
     * @param mcc Mobile Country Code
     * @see [IGracenoteService.setMcc]
     */
    override fun setMcc(mcc: Int) {
        service.setMcc(mcc)
    }

    /**
     * @param country two letter country code
     * @see [IGracenoteService.setCountryOfSale]
     */
    override fun setCountryOfSale(country: String) {
        service.setCountryOfSale(country)
    }

    /**
     * @see [IGracenoteService.getGnSdkVersionInfo]
     */
    override fun getGnSdkVersionInfo(): String {
        return service.getGnSdkVersionInfoKt(dataDir)
    }

    override fun initializeGnService() {
        val fwInitializer = FrameworkConfigUnpacker()
        fwInitializer.unpackConfigFiles(dataDir)

        MediaCodingValues.countryOfSales?.countryCode.let { cc ->
            if (cc != null && cc != "--") {
                log.info(this::initializeGnService.name) { "set country code to: cc" }
                service.setCountryOfSale(cc)
            } else {
                log.error(this::initializeGnService.name) {
                    "country code not available - gracenote functionality unavailable"
                }
            }
        }

        service.initializeGnService(dataDir, locale)
    }

    /**
     * @see [IGracenoteService.doTextLookupForAlbum]
     */
    override fun doTextLookupForAlbum(
        title: String?,
        artist: String?,
        album: String?,
        callback: ICoverArtLookupCallback
    ) {
        service.doTextLookupForAlbumKt(title, artist, album, dataDir, callback)
    }

    override fun requestLyrics(
        type: GnLyricsType,
        familyFriendly: Boolean,
        callback: ISongLyricsCallback
    ) {
        service.requestLyricsKt(type, familyFriendly, callback)
    }

    /**
     * @see [IGracenoteService.startSongRecognition]
     */
    override fun startSongRecognition(fd: Int) {
        service.startSongRecognitionKt(
            dataDir,
            fd,
            recordingConfig.sampleRate,
            recordingConfig.encoding.sampleSize,
            recordingConfig.channels.channelCount
        )
    }

    /**
     * @see [IGracenoteService.stopSongRecognition]
     */
    override fun stopSongRecognition() {
        service.stopSongRecognitionKt(dataDir)
    }

    /**
     * @see [IGracenoteService.requestRadioStationData]
     */
    override fun requestRadioStationData(
        positionData: PositionData,
        stationInfo: RadioStationInfo,
        countryCode: Int,
        callback: IRadioStationCallback
    ) {
        radioStationCallbacks.remove(stationInfo)?.onRadioStationData("", "", byteArrayOf())

        radioStationCallbacks[stationInfo] = callback
        service.requestRadioStationDataKt(dataDir, positionData, stationInfo, countryCode)
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionResult]
     */
    @Suppress("MagicNumber")
    @Deprecated(
        "Superseded by OO style function.",
        ReplaceWith("onRecognitionResult(TrackInfo, ByteArray)")
    )
    override fun onRecognitionResult(metadata: Array<String>, imgData: ByteArray) {
        if (metadata.isEmpty()) {
            log.warn("onRecognitionResult") {
                "received empty legacy recognition metadata array"
            }
            return
        }

        val recognitionResult = SongRecognitionResult(
            title = metadata.getOrNull(0),
            album = metadata.getOrNull(1),
            artist = metadata.getOrNull(2),
            genre = metadata.getOrNull(3),
            year = metadata.getOrNull(4),
            image = imgData
        )

        log.info("onRecognitionResult") {
            "received legacy metadata=$recognitionResult"
        }

        sendSongRecognitionResult(recognitionResult)
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionResult]
     */
    override fun onRecognitionResult(metadata: TrackInfo, imgData: ByteArray) {
        val recognitionResult = SongRecognitionResult(
            title = metadata.trackName,
            album = metadata.album,
            artist = metadata.artist,
            genre = metadata.genre,
            year = metadata.year,
            mood = metadata.mood,
            tempo = metadata.tempo,
            externalIds = metadata.externalIdList.filter {
                !it.source.isNullOrEmpty() && !it.value.isNullOrEmpty()
            },
            externalAlbumIds = metadata.externalAlbumIdList.filter {
                !it.source.isNullOrEmpty() && !it.value.isNullOrEmpty()
            },
            gracenoteId = metadata.gnId,
            gracenoteUniqueId = metadata.gnUId,
            image = imgData
        )

        log.info("onRecognitionResult") { "received metadata=$recognitionResult" }

        sendSongRecognitionResult(recognitionResult)
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionClearResult]
     */
    override fun onRecognitionClearResult() {
        sendSongRecognitionResult(SongRecognitionResult())
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionStatus]
     */
    override fun onRecognitionStatus(recognitionStatus: GnRecognitionStatus) {
        sendSongRecognitionStatus(recognitionStatus)
    }

    private fun sendSongRecognitionResult(songRecognitionResult: SongRecognitionResult) {
        songRecognitionCallbacks.forEach {
            it.onRecognized(songRecognitionResult)
        }
    }

    private fun sendSongRecognitionStatus(recognitionStatus: GnRecognitionStatus) {
        songRecognitionCallbacks.forEach {
            it.onStatus(recognitionStatus)
        }
    }

    /**
     * @see [IGracenoteServiceCallback.onRadioStationResult]
     */
    override fun onRadioStationResult(stationdata: RadioStationInfo, imgData: ByteArray) {
        log.info("onRadioStationResult") {
            "received result=$stationdata image size=${imgData.size}B"
        }

        val name = stationdata.Name
        val id = stationdata.StationId
        if (name != null && id != null) {
            radioStationCallbacks.remove(stationdata)?.onRadioStationData(name, id, imgData)
        }
    }
}

