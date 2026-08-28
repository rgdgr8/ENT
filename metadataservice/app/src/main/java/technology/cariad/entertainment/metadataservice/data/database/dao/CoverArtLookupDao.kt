package technology.cariad.entertainment.metadataservice.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.data.database.pojo.CoverArtLookupWithImageAndLifeCycleData
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity

@Dao
abstract class CoverArtLookupDao : BaseDao<CoverArtLookupEntry>() {

    internal fun createCoverArtLookupEntry(
        albumId: Long?,
        artistId: Long?,
        imageId: Long?,
        lifeCycleDataId: Long,
        coverArtRequestData: CoverArtRequestDataEntity
    ) = CoverArtLookupEntry(
        albumId = albumId,
        artistId = artistId,
        imageId = imageId,
        lifeCycleDataId = lifeCycleDataId,
        trackName = coverArtRequestData.title
    )

    @Transaction
    @Query("SELECT * FROM cover_art_lookup WHERE album_id = :albumId AND artist_id = :artistId")
    internal abstract suspend fun findByAlbumIdAndArtistId(
        albumId: Long,
        artistId: Long
    ): CoverArtLookupWithImageAndLifeCycleData?

    @Transaction
    @Query("SELECT * FROM cover_art_lookup WHERE artist_id = :artistId AND track_name = :title")
    internal abstract suspend fun findByArtistAndTrack(
        artistId: Long,
        title: String
    ): CoverArtLookupWithImageAndLifeCycleData?

    @Transaction
    @Query("SELECT * FROM cover_art_lookup WHERE cover_art_lookup_id = :coverArtLookupId ")
    internal abstract suspend fun findById(
        coverArtLookupId: Long
    ): CoverArtLookupWithImageAndLifeCycleData?

    @Transaction
    @Query("SELECT * FROM cover_art_lookup WHERE life_cycle_data_id = :lifeCycleDataId LIMIT 1")
    internal abstract suspend fun findByLifeCycleDataId(
        lifeCycleDataId: Long
    ): CoverArtLookupWithImageAndLifeCycleData?

    @Query("DELETE FROM cover_art_lookup WHERE cover_art_lookup_id = :coverArtLookupId")
    internal abstract suspend fun deleteById(coverArtLookupId: Long): Int

    @Query("SELECT COUNT(cover_art_lookup_id) FROM cover_art_lookup")
    internal abstract suspend fun getCount(): Int

    @Transaction
    @Query(
        "SELECT cover_art_lookup.cover_art_lookup_id FROM cover_art_lookup INNER JOIN " +
                "life_cycle_data ON cover_art_lookup.life_cycle_data_id = " +
                "life_cycle_data.life_cycle_data_id " +
                "ORDER BY life_cycle_data.last_requested ASC LIMIT 1"
    )
    internal abstract suspend fun findLeastRecentlyUsedId(): Long?
}


