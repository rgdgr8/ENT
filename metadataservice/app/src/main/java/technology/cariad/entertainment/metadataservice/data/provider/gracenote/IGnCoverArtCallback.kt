package technology.cariad.entertainment.metadataservice.data.provider.gracenote

fun interface IGnCoverArtCallback {

    /**
     * New cover art as result of a text based lookup is available.
     *
     * @param albumInfo Metadata result for album
     * @param imgData   Raw image data.
     */
    fun callback(albumInfo: AlbumInfo?, imgData: ByteArray?)
}
