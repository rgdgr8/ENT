package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * Callback interface for station data and logo as response to radio station data requests.
 */
interface IRadioStationCallback {

    /**
     * Called when a result is available.
     * @param name station name
     * @param providerId provider specific identifier for this station
     * @param imgData Raw byte data of the image.
     */
    fun onRadioStationData(name: String, providerId: String, imgData: ByteArray)
}

