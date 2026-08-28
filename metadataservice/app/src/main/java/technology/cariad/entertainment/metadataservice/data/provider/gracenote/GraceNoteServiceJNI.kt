package de.esolutions.gracenoteservice.gntest

import androidx.annotation.Keep
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.loadLibrary
import technology.cariad.entertainment.metadataservice.data.provider.GnServiceRequestNr
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.AlbumInfo
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.GnLyricsType
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.GnRecognitionStatus
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.ICoverArtLookupCallback
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGnCoverArtCallback
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGnLyricsCallback
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGracenoteServiceCallback
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.ISongLyricsCallback
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.PositionData
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.RadioStationInfo
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.TrackInfo

typealias GraceNoteServiceJNI = MainActivity

// TODO: package and class name are predetermined by the current gn_service implementation and
// must not be changed here. Otherwise the external functions can't be resolved. Rework as soon
// as Swig generated JNI interface is available.
@Suppress("MatchingDeclarationName")
class MainActivity(private val callback: IGracenoteServiceCallback) {

    companion object {
        init {
            loadLibrary("jni-gracenoteservice")
        }
    }

    private val log by logger(GraceNoteServiceJNI::class)

    private val apnMap = mutableMapOf<GnServiceRequestNr, String>()

    /**
     * @see
     *[technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGracenoteService.updateApnConfigEntry]
     *
     */
    fun updateApnConfigEntry(requestNr: GnServiceRequestNr, networkInterface: String?) {
        if (networkInterface != null) {
            apnMap[requestNr] = networkInterface
        } else {
            apnMap.remove(requestNr)
        }

        log.info(this::updateApnConfigEntry.name) {
            "setting new apn config: ${
                apnMap.entries.map {
                    "(${it.key},${it.value})"
                }.reduceOrNull() { acc, s ->
                    "$acc $s"
                } ?: "empty"
            }"
        }

        setApnConfig(apnMap)
    }

    /**
     * native methods implemented by the 'gnservice-lib' native library,
     * which is packaged with this application.
     */

    /**
     * Returns the Gracenote SDK version.
     * @param dir Gracenote database directory
     * @return Gracenote SDK version if found, otherwise null
     */
    external fun getGnsdkVersionInfo(dir: String?): String?

    /**
     * initialize the gracenote service (GnService)
     * @param dir Gracenote database directory, also used for internal gnsdk files such as user etc.
     * @param locale android system language setting used for initialize localization within gnsdk
     *               it is expected in the form as in 'en_US', two letter language, two letter
     *               country which can be retrieved using
     *               Locale().getDefault().getLanguage() + "_" +
     *               Locale().getDefault().getCountry()
     */
    external fun initializeGnService(dir: String?, locale: String?)

    /**
     * Does text look up for album.
     * @param title track title
     * @param artist track artist
     * @param album track album
     * @param dir Gracenote database directory
     */
    external fun doTextLookupForAlbum(
        title: String?,
        artist: String?,
        album: String?,
        dir: String?,
        callback: IGnCoverArtCallback
    )

    /**
     * Starts the song recognition.
     * @param dir Gracenote database directory
     * @param fileDescriptor File handle the audio data can be read from
     * @param sampleRate Number of audio samples per second
     * @param bitRate Size of each individual audio sample
     * @param channels Channel count, 1 for mono and 2 for stereo
     */
    external fun startSongRecognition(
        dir: String?,
        fileDescriptor: Int,
        sampleRate: Int,
        bitRate: Int,
        channels: Int
    )

    /**
     * Stops the song recognition.
     * @param dir Gracenote database directory
     */
    external fun stopSongRecognition(dir: String?)

    /**
     * Sets APN config with given Gracenote service request number and network interface name.
     * @param config Map of request number and network interface name
     */
    external fun setApnConfig(config: Map<GnServiceRequestNr, String>)

    /**
     * Requests radio station data.
     * @param dir Radio station data directory.
     * @param positionData Position data including latitude and longitude
     * @param stationInfo Radio station data
     * @param countryCode Mobile country code (mcc)
     */
    external fun requestRadioStationData(
        dir: String?,
        positionData: PositionData,
        stationInfo: RadioStationInfo,
        countryCode: Int
    )

    /**
     * Requests lyrics for currently recognized song
     * @param type full lyrics (static), line by line, or word by word lyrics
     * @param familyFriendly defines if lyrics should be family friendly (no vulgar words) or not
     * @param callback to be called with lyrics result
     */
    external fun requestLyrics(
        type: Int,
        familyFriendly: Boolean,
        callback: IGnLyricsCallback
    )

    /**
     * Sets the Mcc (mobile country code) within the GracenoteService to be used in gnsdk for Lyrics
     * @param mcc mobile country code to be set
     */
    external fun setMcc(
        mcc: Int
    )

    /**
     * Set country of sale within the GracenoteService to decide whether the china proxy needs
     * to be set up for all gracenote requests
     */
    external fun setCountryOfSale(
        country: String
    )

    /**
     * Kotlin wrapper around external function.
     * @see [getGnsdkVersionInfo]
     */
    fun getGnSdkVersionInfoKt(dir: String): String {
        return getGnsdkVersionInfo(dir) ?: "unknown"
    }

    /**
     * Kotlin wrapper around external function.
     * @see [getGnsdkVersionInfo]
     */
    fun initializeGnServiceKt(dir: String, locale: String) {
        return initializeGnService(dir, locale)
    }

    /**
     * Kotlin wrapper around external function.
     * @see [doTextLookupForAlbum]
     *
     */
    fun doTextLookupForAlbumKt(
        title: String?,
        artist: String?,
        album: String?,
        dir: String?,
        callback: ICoverArtLookupCallback
    ) {
        doTextLookupForAlbum(title, artist, album, dir) { _: AlbumInfo?, imgData: ByteArray? ->
            callback.onCoverArtReceived(imgData)
        }
    }


    /** Kotlin wrapper around external function
     * @see [requestLyrics]
     */
    fun requestLyricsKt(
        type: GnLyricsType,
        familyFriendly: Boolean,
        callback: ISongLyricsCallback
    ) {
        requestLyrics(type.ordinal, familyFriendly) { playPosition: Int, lyricsData: String? ->
            callback.onLyricsReceived(playPosition, lyricsData)
        }
    }

    /**
     * Kotlin wrapper around external function.
     * @see [startSongRecognition]
     */
    fun startSongRecognitionKt(
        dir: String?,
        fileDescriptor: Int,
        sampleRate: Int,
        bitRate: Int,
        channels: Int
    ) {
        startSongRecognition(dir, fileDescriptor, sampleRate, bitRate, channels)
    }

    /**
     * Kotlin wrapper around external function.
     * @see [stopSongRecognition]
     */
    fun stopSongRecognitionKt(dir: String?) {
        stopSongRecognition(dir)
    }

    /**
     * Kotlin wrapper around external function.
     * @see [requestRadioStationData]
     */
    fun requestRadioStationDataKt(
        dir: String?,
        positionData: PositionData,
        stationInfo: RadioStationInfo,
        countryCode: Int
    ) {
        requestRadioStationData(dir, positionData, stationInfo, countryCode)
    }

    /**
     * native callback definitions
     */

    /**
     * @see [IGracenoteServiceCallback.onEventCoverArtReceived]
     */
    @Deprecated(
        "Superseded by callback passed to doTextLookupForAlbum.",
        ReplaceWith("no direct replacement available")
    )
    @Keep
    fun onEventCoverArtReceived(imgData: ByteArray) {
        log.warn(this::onEventCoverArtReceived.name) {
            "method not implemented"
        }
    }

    /**
     * @see [IGracenoteServiceCallback.onEventMetadataReceived]
     */
    @Deprecated(
        "Superseded by callback passed to doTextLookupForAlbum.",
        ReplaceWith("no direct replacement available")
    )
    @Keep
    fun onEventMetadataReceived(albumMetadata: Array<String>) {
        log.warn(this::onEventCoverArtReceived.name) {
            "method not implemented"
        }
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionResult]
     */
    @Keep
    fun onRecognitionDataReceived(albumMetadata: Array<String>, imgData: ByteArray) {
        log.info(this::onRecognitionDataReceived.name) {
            "received album info: ${albumMetadata.reduce { acc, s -> "$acc $s" }}\n" +
                    "received image with ${imgData.size} bytes\""
        }

        callback.onRecognitionResult(albumMetadata, imgData)
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionResult]
     */
    @Keep
    fun onNewRecognitionDataReceived(trackMetadata: TrackInfo, imgData: ByteArray) {
        callback.onRecognitionResult(trackMetadata, imgData)
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionClearResult]
     */
    @Keep
    fun onRecognitionClearResult() {
        log.info(this::onRecognitionClearResult.name) {
            "clear result"
        }

        callback.onRecognitionClearResult()
    }

    /**
     * @see [IGracenoteServiceCallback.onRecognitionStatus]
     */
    @Keep
    fun onRecognitionStatus(recognitionStatus: Int) {

        val status = GnRecognitionStatus.getByValue(recognitionStatus)

        log.info(this::onRecognitionStatus.name) {
            "update state=$status"
        }

        callback.onRecognitionStatus(status)
    }

    /**
     * @see [IGracenoteServiceCallback.onRadioStationResult]
     */
    @Keep
    fun onRadioStationDataReceived(stationInfo: RadioStationInfo, imgData: ByteArray) {
        log.info(this::onRadioStationDataReceived.name) {
            "recived station data info=$stationInfo image size=${imgData.size}"
        }

        callback.onRadioStationResult(stationInfo, imgData);
    }
}

