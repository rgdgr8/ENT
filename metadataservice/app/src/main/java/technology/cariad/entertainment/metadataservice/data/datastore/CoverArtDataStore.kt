package technology.cariad.entertainment.metadataservice.data.datastore

import androidx.room.withTransaction
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.database.DatabaseConstants
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.AlbumStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.ArtistStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.CoverArtStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.ImageStore
import technology.cariad.entertainment.metadataservice.data.datastore.coverart.LifeCycleStore
import technology.cariad.entertainment.metadataservice.domain.ICoverArtDataStore
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import java.io.IOException

class CoverArtDataStore(
    private val database: CachingDatabase,
    private val coverArtStore: CoverArtStore,
    private val albumStore: AlbumStore,
    private val artistStore: ArtistStore,
    private val imageStore: ImageStore,
    private val imageDataStore: IImageDataStore,
    private val lifeCycleStore: LifeCycleStore
) : ICoverArtDataStore {

    private val log by logger()

    /**
     * @see [ICoverArtDataStore.getCoverArt]
     */
    override suspend fun getCoverArt(
        coverArtRequestDataEntity: CoverArtRequestDataEntity
    ): ImageEntity? =
        database.withTransaction {
            val title = coverArtRequestDataEntity.title
            val albumId = coverArtRequestDataEntity.album?.let { albumStore.getAlbumIdByName(it) }
            val artistId =
                coverArtRequestDataEntity.artist?.let { artistStore.getArtistIdByName(it) }
            val completeCoverArtLookup =
                coverArtStore.getCoverArtLookup(albumId, artistId, title)
                    ?: return@withTransaction null
            val newLifeCycleEntry = lifeCycleStore.createLifeCycleDataEntry(
                completeCoverArtLookup.lifeCycleData.lifeCycleDataId,
                coverArtRequestDataEntity.lifeCycleData!!
            )
            lifeCycleStore.updateLifeCycleData(newLifeCycleEntry)

            val coverArt = completeCoverArtLookup.coverArt

            if (coverArt != null) {
                val imageUri = imageDataStore.loadImageUri(coverArt.fileName)
                ImageEntity(
                    imageData = null,
                    uri = imageUri,
                    fileName = coverArt.fileName,
                    imageType = ImageEntity.Companion.ImageType.COVER_ART,
                    eTag = null
                )
            } else {
                null
            }
        }

    /**
     * @see [ICoverArtDataStore.addCoverArtLookup]
     */
    override suspend fun addCoverArtLookup(
        coverArtRequestDataEntity: CoverArtRequestDataEntity,
        imageEntity: ImageEntity
    ): String? =
        try {
            database.withTransaction {
                val lifeCycleData =
                    coverArtRequestDataEntity.lifeCycleData ?: return@withTransaction null

                val coverArtCount = coverArtStore.getCoverArtLookupCount()

                if (coverArtCount >= DatabaseConstants.COVER_ART_CACHE_SIZE) {
                    log.info(this::addCoverArtLookup.name) {
                        "cover art limit is exceeded: cached " +
                                "${DatabaseConstants.COVER_ART_CACHE_SIZE} " +
                                "out of $coverArtCount  cover arts"
                    }

                    val coverArtLookupId = coverArtStore.findLeastRecentlyUsedId()

                    if (coverArtLookupId == null) {
                        log.error(this::addCoverArtLookup.name) {
                            "cover art cache size limit is exceeded, new cover art" +
                                    " can not be saved without cleaning 1 record from DB!"
                        }
                        return@withTransaction null
                    }

                    val coverArtLookup = coverArtStore.getCoverArtLookupById(coverArtLookupId)

                    if (coverArtLookup == null) {
                        log.error(this::addCoverArtLookup.name) {
                            "cover art cache size limit is exceeded, new cover art " +
                                    "can not be saved without cleaning 1 record from DB!"
                        }
                        return@withTransaction null
                    }

                    coverArtStore.deleteCoverArtById(coverArtLookupId)
                    coverArtLookup.coverArt?.let {
                        if (!imageDataStore.deleteImage(it.fileName)) {
                            throw IOException("Failed to delete ${it.fileName}")
                        }
                    }
                }

                val albumId = coverArtRequestDataEntity.album?.let {
                    albumStore.selectOrInsertAlbum(it)
                }
                val artistId = coverArtRequestDataEntity.artist?.let {
                    artistStore.selectOrInsertArtist(it)
                }
                val imageEntry = imageStore.createImageEntry(imageEntity)
                val imageId = imageStore.insertImage(imageEntry)
                val newLifeCycleEntry = lifeCycleStore.createLifeCycleDataEntry(
                    lifeCycleDataEntity = lifeCycleData
                )
                val lifeCycleDataId =
                    lifeCycleStore.insertLifeCycleData(newLifeCycleEntry)
                val coverArtLookupEntry = coverArtStore.getCoverArtLookupEntry(
                    albumId = albumId,
                    artistId = artistId,
                    imageId = imageId,
                    lifeCycleDataId = lifeCycleDataId,
                    coverArtRequestData = coverArtRequestDataEntity
                )
                coverArtStore.insertCoverArtLookupEntry(coverArtLookupEntry)

                val uri = imageDataStore.saveImage(
                    imageEntity.imageData!!.toByteArray(), imageEntity.fileName
                )

                if (uri.isNullOrEmpty()) {
                    throw IOException("Failed to save ${imageEntity.fileName}")
                }
                uri
            }
        } catch (e: IOException) {
            log.error(this::addCoverArtLookup.name) {
                "${e.message}"
            }
            null
        }
}

