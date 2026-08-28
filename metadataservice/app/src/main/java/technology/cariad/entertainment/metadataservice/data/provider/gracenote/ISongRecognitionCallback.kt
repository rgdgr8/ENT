package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * Callback interface for the song recognition..
 */
interface ISongRecognitionCallback {

    /**
     * Called when a new song recognition result is available.
     * Result may be empty to indicate the end of a track or no music playing at all.
     * This allows to clear any displayed metadata instead of showing incorrect or outdated
     * information.
     * @param result The metadata of the recognized song.
     */
    fun onRecognized(result: SongRecognitionResult)

    /**
     * Called when the state of the song recognition changed within the implementing service.
     * This status indicates if the service is ready to process audio data or not.
     */
    fun onStatus(status: GnRecognitionStatus)
}

