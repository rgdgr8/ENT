package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.CoverArtLookupDao
import technology.cariad.entertainment.metadataservice.data.database.entity.CoverArtLookupEntry
import technology.cariad.entertainment.metadataservice.data.database.pojo.CoverArtLookupWithImageAndLifeCycleData
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity

class CoverArtStore(private val coverArtLookupDao: CoverArtLookupDao) {

    internal suspend fun getCoverArtLookupCount(): Int {
        return coverArtLookupDao.getCount()
    }

    internal suspend fun findLeastRecentlyUsedId(): Long? {
        return coverArtLookupDao.findLeastRecentlyUsedId()
    }

    internal suspend fun getCoverArtLookupById(
        coverArtLookupId: Long
    ): CoverArtLookupWithImageAndLifeCycleData? {
        return coverArtLookupDao.findById(coverArtLookupId)
    }

    internal suspend fun deleteCoverArtById(coverArtLookUpId: Long): Int {
        return coverArtLookupDao.deleteById(coverArtLookUpId)
    }

    /**
     * Gets the cover art lookup entry associated with the given parameters.
     * @param albumId album id
     * @param artistId artist id
     * @param lifeCycleDataId
     * @param coverArtRequestData
     * @return [CoverArtLookupEntry]
     */
    internal fun getCoverArtLookupEntry(
        albumId: Long?,
        artistId: Long?,
        imageId: Long?,
        lifeCycleDataId: Long,
        coverArtRequestData: CoverArtRequestDataEntity
    ): CoverArtLookupEntry {
        return coverArtLookupDao.createCoverArtLookupEntry(
            albumId,
            artistId,
            imageId,
            lifeCycleDataId,
            coverArtRequestData
        )
    }

    internal suspend fun insertCoverArtLookupEntry(coverArtLookupEntry: CoverArtLookupEntry): Long {
        return coverArtLookupDao.insert(coverArtLookupEntry)
    }

    internal suspend fun getCoverArtLookup(
        albumId: Long?, artistId: Long?, title: String?
    ): CoverArtLookupWithImageAndLifeCycleData? {
        return if (albumId != null && artistId != null) {
            coverArtLookupDao.findByAlbumIdAndArtistId(
                albumId = albumId,
                artistId = artistId
            )
        } else if (artistId != null && !title.isNullOrEmpty()) {
            coverArtLookupDao.findByArtistAndTrack(artistId, title)
        } else {
            null
        }
    }
}

