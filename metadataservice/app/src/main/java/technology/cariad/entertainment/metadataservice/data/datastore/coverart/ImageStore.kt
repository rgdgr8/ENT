package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity

class ImageStore(private val imageDao: ImageDao) {

    internal fun createImageEntry(imageEntity: ImageEntity): ImageEntry {
        return imageDao.createImageEntry(image = imageEntity)
    }

    internal suspend fun insertImage(imageEntry: ImageEntry): Long {
        return imageDao.insert(imageEntry)
    }

    internal suspend fun deleteById(imageId: Long) {
         imageDao.deleteById(imageId)
    }
}

