package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import technology.cariad.entertainment.metadataservice.data.database.entity.ArtistEntry

@Dao
abstract class ArtistDao : BaseDao<ArtistEntry>() {

    internal fun createArtistEntry(artistName: String) = ArtistEntry(artistName = artistName)

    internal suspend fun selectOrInsertArtist(artistName: String): Long {
        var id = findArtistIdByName(artistName)

        if (id == null) {
            val artistEntry = createArtistEntry(artistName)
            id = insert(artistEntry)
            assert(id != -1L)
        }
        return id
    }

    @Query("SELECT artist_id FROM artist WHERE artist_name = :artistName")
    internal abstract suspend fun findArtistIdByName(artistName: String): Long?
}
