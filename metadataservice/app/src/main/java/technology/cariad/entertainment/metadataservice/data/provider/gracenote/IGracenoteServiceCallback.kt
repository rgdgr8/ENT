package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * Callback interface for Gracenote service events.
 */
interface IGracenoteServiceCallback {

    /**
     * New metadata and image as result of the song recognition is available.
     * @param metadata Fields in the following order:
     *                 1. Trackname (Titel)
     *                 2. Album
     *                 3. Artist
     *                 4. Genre
     *                 5. Year
     * @param imgData Raw image data.
     */
    fun onRecognitionResult(metadata: Array<String>, imgData: ByteArray)

    /**
     * New metadata and image as result of the song recognition is available.
     * @param metadata All available metadata fields
     * @param imgData raw image data for cover art
     */
    fun onRecognitionResult(metadata: TrackInfo, imgData: ByteArray);

    /**
     * Invalidates the last song recognition result.
     * Called if a track change occurred or no music is playing.
     */
    fun onRecognitionClearResult()

    /**
     * handles the general status of song recognition
     */
    fun onRecognitionStatus(recognitionStatus: GnRecognitionStatus)

    /**
     * New radio station info and image as result of a radio station data request are available.
     * @param stationdata Available metadata
     * @param imgData raw image data for station logo
     */
    fun onRadioStationResult(stationdata: RadioStationInfo, imgData: ByteArray)
}

