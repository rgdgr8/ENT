package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.common.util.toBearerString
import technology.cariad.entertainment.metadataservice.data.database.dao.BearerDao
import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity

class BearerStore(private val bearerDao: BearerDao, private val eTagsDao: ETagsDao) {

    /**
     * Searches for a station associated with [bearer].
     * @param bearer
     * @return station id or null, if no station was found
     */
    internal suspend fun getStationIdByBearer(bearer: IBearerEntity): Long? {
        val bearerUri = bearer.toBearerString()!!
        return bearerDao.findStationIdByBearer(bearerUri)
    }

    /**
     * Removes the station associated with [stationId].
     */
    internal suspend fun removeBearersByStationId(stationId: Long) {
        bearerDao.deleteByStationId(stationId)
        eTagsDao.updateBearerListETag(stationId, "")
    }

    /**
     * Inserts [bearers] for the station associated with [stationId].
     */
    internal suspend fun insertBearers(
        stationId: Long,
        bearerList: BearerListEntity
    ): List<Long> {
        eTagsDao.updateBearerListETag(stationId, bearerList.etag ?: "")
        val bearerEntries = bearerDao.createBearerEntries(bearerList, stationId)
        return bearerDao.insertIgnore(bearerEntries)
    }

    /**
     * Updates the list of alternative bearers for the given station.
     * If alternative bearers are already stored they are replaced entirely with the new bearers.
     * @param stationId Requested station
     * @param bearerList
     * @param etag
     */
    internal suspend fun updateBearers(
        stationId: Long,
        bearersList: BearerListEntity
    ) {
        val uris = bearersList.bearers.mapNotNull { it.toBearerString() }
        bearerDao.deleteMissingBearersForStationId(stationId, uris)
        insertBearers(stationId, bearersList)
    }
}

