package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.data.database.DatabaseConstants
import technology.cariad.entertainment.metadataservice.data.database.dao.BearerDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ImageDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationImageDao
import technology.cariad.entertainment.metadataservice.data.database.entity.BearerEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.ETagsEntry
import technology.cariad.entertainment.metadataservice.data.database.pojo.CompleteRadioStation
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity

private const val INVALID_DATA_INDEX = -1L

class StationStore(
    private val stationDao: StationDao,
    private val bearerDao: BearerDao,
    private val stationImageDao: StationImageDao,
    private val imageDao: ImageDao,
    private val imageDataStore: IImageDataStore,
    private val eTagsDao: ETagsDao
) {
    private val log by logger()

    /**
     * Creates a station or returns the id of an existing one.
     */
    internal suspend fun getOrCreateStation(bearer: IBearerEntity): Long {
        val bearerUri = bearer.toBearerString()!!
        var stationId = bearerDao.findStationIdByBearer(bearerUri)

        if (stationId == null) {
            log.info(this::getOrCreateStation.name) {
                "no station found, new station should be created"
            }
            stationId = createStation(bearerUri)
        }
        return stationId
    }

    internal suspend fun createStation(bearerUri: String): Long {
        val stationCount = stationDao.getCount()

        log.info(this::getOrCreateStation.name) {
            "cached $stationCount out of ${DatabaseConstants.STATION_LOGO_CACHE_SIZE} stations"
        }

        //TODO: cache size configuration will need rework!
        if (stationCount >= DatabaseConstants.STATION_LOGO_CACHE_SIZE) {
            val oldestStationId = stationDao.findLeastRecentlyUsedId()

            if (oldestStationId == null) {
                log.error(this::getOrCreateStation.name) {
                    "radio station cache size limit is exceeded, new radio station can " +
                            "not be saved without cleaning 1 record from DB!"
                }
                return INVALID_DATA_INDEX
            } else {
                if (!deleteCompleteStation(oldestStationId)) return INVALID_DATA_INDEX
            }
        }

        // At this point we dont have a lifecycle object.
        // Time provider is also not at that level, we can t create lastRequested,
        // so leave it optional!
        val stationEntry = stationDao.createStationEntry()
        val stationId = stationDao.insert(stationEntry)
        bearerDao.insert(BearerEntry(stationId = stationId, uri = bearerUri))
        eTagsDao.insert(ETagsEntry(stationId, "", "", "", "", ""))

        log.info(this::getOrCreateStation.name) {
            "new station is created with id: $stationId uri: $bearerUri, new station " +
                    "and bearer entries are inserted into DB"
        }
        return stationId
    }

    internal suspend fun deleteCompleteStation(stationId: Long): Boolean {
        val imageIds = stationImageDao.findImageIdsByStationId(stationId)
        val imageNames = imageIds.mapNotNull { imageDao.findImageById(it)?.fileName }

        var success = imageNames.isEmpty()

        stationDao.deleteByStationId(stationId)
        imageNames.forEach { imageName ->
            val imageIdsForSameFileNames = imageDao.findImageIdsByFileName(imageName)
            if (imageIdsForSameFileNames.isNotEmpty()) {
                log.info(this::deleteCompleteStation.name) {
                    "image : $imageName used by other image IDs: [$imageIdsForSameFileNames]"
                }
                success = true;
                return@forEach
            }

            imageDataStore.deleteImage(imageName).also { success = it }
            if (!success) {
                return false
            }
            log.info(this::deleteCompleteStation.name) {
                "image : $imageName deleted from file system."
            }
        }
        log.info(this::deleteCompleteStation.name) {
            "delete the oldest station id= $stationId"
        }
        checkAndPruneRedundantLogos()
        return success
    }

    internal suspend fun getCompleteById(stationId: Long): CompleteRadioStation? {
        return stationDao.findCompleteById(stationId)
    }

    private suspend fun checkAndPruneRedundantLogos() {
        val fileCountInLogoCache = imageDataStore.getImageFilesCount()
        val radioLogosInDatabase =
            imageDao.getUniqueFileNamesOfType(ImageEntity.Companion.ImageType.LOGO.value)
        if ((fileCountInLogoCache != 0) && (fileCountInLogoCache > radioLogosInDatabase.size)) {
            log.info(this::checkAndPruneRedundantLogos.name) {
                "Redundant files in cache ($fileCountInLogoCache/${radioLogosInDatabase.size})," +
                        " removing them."
            }

            val fileCountDifference = fileCountInLogoCache - radioLogosInDatabase.size
            imageDataStore.pruneImageFiles(radioLogosInDatabase, fileCountDifference)
        }
    }
}

