package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationImageDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType

class ImageStore(
    private val stationImageDao: StationImageDao,
    private val imageDao: ImageDao,
    private val eTagsDao: ETagsDao
) {
    private val log by logger()

    internal suspend fun findLogoByStationIdAndType(
        stationId: Long,
        imageType: ImageType
    ): Long? {
        return stationImageDao.findLogoByStationIdAndType(stationId, imageType)
    }

    internal fun createStationImageEntry(
        imageId: Long,
        stationId: Long
    ): StationImageEntry {
        return stationImageDao.createStationImageEntry(imageId, stationId)
    }

    internal suspend fun delete(stationImageEntry: StationImageEntry, type: ImageType) {

        when (type) {
            ImageType.LOGO -> eTagsDao.updateLogoETag(stationImageEntry.stationId, "")
            ImageType.LOGO_SMALL -> eTagsDao.updateLogoSmallETag(stationImageEntry.stationId, "")
            else -> log.error(this::delete.name) { "invalid image type for radio station: $type" }
        }

        stationImageDao.delete(stationImageEntry)
    }


/*    internal suspend fun deleteById(imageId: Long) {
        imageDao.deleteById(imageId)
    }*/

    internal suspend fun getImageEntryById(imageId: Long): ImageEntry? {
        return imageDao.findImageById(imageId)
    }

    internal suspend fun getImageIdsByImageName(imageName: String): List<Int> {
        return imageDao.findImageIdsByFileName(imageName)
    }

    internal suspend fun updateLogo(stationId: Long, logo: ImageEntity) {

        val logoId = stationImageDao.findLogoByStationIdAndType(stationId, logo.imageType!!)

        if (logoId == null) {
            val imageEntry = imageDao.createImageEntry(0, logo)
            val imageId = imageDao.insert(imageEntry)
            val stationImageEntry = stationImageDao.createStationImageEntry(imageId, stationId)
            stationImageDao.insert(stationImageEntry)
        } else {
            val logoEntry = imageDao.createImageEntry(logoId, logo)
            imageDao.update(logoEntry)
        }

        when (logo.imageType) {
            ImageType.LOGO -> eTagsDao.updateLogoETag(stationId, logo.eTag ?: "")
            ImageType.LOGO_SMALL -> eTagsDao.updateLogoSmallETag(stationId, logo.eTag ?: "")
            else -> log.error(this::delete.name) {
                "invalid image type for radio station: ${logo.imageType}"
            }
        }
    }
}

