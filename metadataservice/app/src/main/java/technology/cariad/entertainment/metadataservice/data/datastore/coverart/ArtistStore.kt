package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.ArtistDao

class ArtistStore(private val artistDao: ArtistDao) {

    /**
     * Searches for an artist associated with [name].
     * @param name
     * @return artist id or null, if no artist was found
     */
    internal suspend fun getArtistIdByName(name: String): Long? {
        return artistDao.findArtistIdByName(name)
    }

    /**
     * Selects or inserts an artist associated with [name] and returns it's id.
     * @param name
     * @return artist id
     */
    internal suspend fun selectOrInsertArtist(name: String): Long {
        return artistDao.selectOrInsertArtist(name)
    }
}

