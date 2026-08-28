package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.AlbumEntry

@Dao
abstract class AlbumDao : BaseDao<AlbumEntry>() {

    internal fun createAlbumEntry(albumName: String) = AlbumEntry(albumName = albumName)

    internal suspend fun selectOrInsertAlbum(albumName: String): Long {
        var id = findAlbumIdByName(albumName)

        if (id == null) {
            val albumEntry = createAlbumEntry(albumName)
            id = insert(albumEntry)
            assert(id != -1L)
        }
        return id
    }

    @Query("SELECT album_id FROM album WHERE album_name = :albumName")
    internal abstract suspend fun findAlbumIdByName(albumName: String): Long?
}
