package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * Callback interface for images as response to cover art lookups.
 */
fun interface ICoverArtLookupCallback {

    /**
     * Called when a  lookup result is available.
     * @param imgData Raw byte data of the image.
     */
    fun onCoverArtReceived(imgData: ByteArray?)
}

