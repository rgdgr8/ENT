package technology.cariad.entertainment.metadataservice.data.provider.gracenote

fun interface IGnLyricsCallback {
    /**
     * New lyrics as result of a lyrics request is available.
     *
     * @param playPosition approximate play position (in milliseconds) in the song at the time the
     *                     lyrics were received
     * @param imgData   lyrics as a String representation.
     */
    fun onLyricsReceived(playPosition: Int, lyricsData: String?)
}
