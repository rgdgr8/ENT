package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.AlbumDao

class AlbumStore(private val albumDao: AlbumDao) {

    /**
     * Searches for an album associated with [name].
     * @param name
     * @return album id or null, if no album was found
     */
    internal suspend fun getAlbumIdByName(name: String): Long? {
        return albumDao.findAlbumIdByName(name)
    }

    /**
     * Selects or inserts an album associated with [name] and returns it's id.
     * @param name
     * @return album id
     */
    internal suspend fun selectOrInsertAlbum(name: String): Long {
        return albumDao.selectOrInsertAlbum(name)
    }
}

