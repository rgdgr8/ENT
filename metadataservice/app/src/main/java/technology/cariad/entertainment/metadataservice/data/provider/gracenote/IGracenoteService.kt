package technology.cariad.entertainment.metadataservice.data.provider.gracenote

import technology.cariad.entertainment.metadataservice.data.provider.GnServiceRequestNr

/**
 * Interface defining the operations offered by the Gracenote service.
 */
interface IGracenoteService {

    /**
     * Queries the version information of the GNSDK.
     * @return version as [String]
     */
    fun getGnSdkVersionInfo(): String

    /**
     * Initializes the GracenoteService.
     */
    fun initializeGnService()

    /**
     * Textual cover art lookup by title or artist or album.
     * Valid combinations are:
     * - title, artist, album
     * - title, artist
     * - title, album
     * - album
     * @param title
     * @param artist
     * @param album
     * @param callback
     */
    fun doTextLookupForAlbum(
        title: String?,
        artist: String?,
        album: String?,
        callback: ICoverArtLookupCallback
    )

    /**
     * Starts the song recognition.
     * @param fd File handle the audio data can be read from.
     *           Usually this is the read end of a pipe.
     * @note It is the responsibility of the callee to close this file descriptor, once it is no
     *       longer needed.
     */
    fun startSongRecognition(fd: Int)

    /**
     * Stops the song recognition.
     */
    fun stopSongRecognition()

    /**
     * Requests radio station data and logo, then returns response through [callback].
     */
    fun requestRadioStationData(
        positionData: PositionData,
        stationInfo: RadioStationInfo,
        countryCode: Int,
        callback: IRadioStationCallback
    )

    /**
     * Requests lyrics from recognized songs, then returns response through [callback].
     */
    fun requestLyrics(
        type: GnLyricsType,
        familyFriendly: Boolean,
        callback: ISongLyricsCallback
    )

    /**
     * Register for notifications of song recognition results.
     */
    fun registerCallback(callback: ISongRecognitionCallback)

    /**
     * Unregister for notifications of song recognition results.
     */
    fun unregisterCallback(callback: ISongRecognitionCallback)

    /**
     * Set the network interface that should be used for a specific request type.
     * @param requestNr The type of the request. Used to potentially route over art requests and the
     *                  song recognition over separate networks.
     * @param networkInterface The name of the network interface.
     *                         May be null if the network is not available.
     */
    fun updateApnConfigEntry(requestNr: GnServiceRequestNr, networkInterface: String?)

    /**
     * Sets the MCC for country specific lyrics requests.
     * @param mcc Mobile Country Code
     */
    fun setMcc(mcc: Int)

    /**
     * Set country of sale within gracenote service in order to decide wether a proxy needs to be
     * set up
     * @param country Country Code for country of sale as defined in ISO-3166 ALPHA-2
     */
    fun setCountryOfSale(country: String)
}

