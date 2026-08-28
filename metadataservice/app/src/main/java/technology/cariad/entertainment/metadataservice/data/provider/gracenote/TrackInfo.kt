package technology.cariad.entertainment.metadataservice.data.provider.gracenote

import java.util.Vector

class TrackInfo internal constructor() {
    var trackName: String? = null
    var artist: String? = null
    var album: String? = null
    var genre: String? = null
    var year: String? = null
    var mood: String? = null
    var tempo: String? = null
    var gnId: String? = null
    var gnUId: String? = null
    var length = 0
    var playposition = 0
    var externalIdList: Vector<ExternalId>
    var externalAlbumIdList: Vector<ExternalId>

    /**
     * Adds new external provider identifier to the list of external providers.
     */
    fun addExternalId(extId: ExternalId) {
        externalIdList.addElement(extId)
    }

    /**
     * Adds new external provider identifier for the found album to the list of external ids.
     */
    fun addExternalAlbumId(extAlbumId: ExternalId) {
        externalAlbumIdList.addElement(extAlbumId)
    }

    init {
        externalIdList = Vector()
        externalAlbumIdList = Vector()
    }
}
