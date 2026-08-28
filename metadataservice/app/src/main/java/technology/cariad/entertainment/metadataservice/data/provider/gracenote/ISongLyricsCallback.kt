package technology.cariad.entertainment.metadataservice.data.provider.gracenote

/**
 * Callback interface for song lyrics.
 */
fun interface ISongLyricsCallback {

    /**
     * Called when new lyrics are available.
     * Result may be empty if the current lyrics are no longer valid or no lyrics could be found.
     * @param playPosition Current play position
     * @param lyricsData Json string containing the lyrics and associated metadata.
     */
    fun onLyricsReceived(playPosition: Int, lyricsData: String?)
}

