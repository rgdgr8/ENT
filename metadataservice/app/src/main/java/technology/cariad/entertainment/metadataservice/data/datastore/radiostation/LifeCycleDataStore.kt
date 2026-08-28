package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.data.database.dao.LifeCycleDataDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationDao
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity

class LifeCycleDataStore(
    private val lifeCycleDataDao: LifeCycleDataDao,
    private val stationDao: StationDao
) {
    private val log by logger()

    internal suspend fun updateLifeCycleData(stationId: Long, lifeCycleData: LifeCycleDataEntity) {
        val stationEntry = stationDao.findByStationId(stationId)
        var lifeCycleDataId = stationEntry.lifeCycleDataId

        if (lifeCycleDataId != null) {
            val lifeCycleDataEntry =
                lifeCycleDataDao.createLifeCycleDataEntry(lifeCycleDataId, lifeCycleData)
            lifeCycleDataDao.update(lifeCycleDataEntry)

            log.info("updateLifeCycleData") {
                "Existing lifeCycleData is updated with Id: $lifeCycleDataId"
            }
        } else {
            val lifeCycleDataEntry =
                lifeCycleDataDao.createLifeCycleDataEntry(lifeCycleData = lifeCycleData)
            lifeCycleDataId = lifeCycleDataDao.insert(lifeCycleDataEntry)
            val newStation = stationDao.createStationEntry(
                stationId = stationId,
                lifeCycleDataId = lifeCycleDataId
            )

            stationDao.update(newStation)

            log.info("updateLifeCycleData") {
                "New lifeCycleData is created with Id: $lifeCycleDataId"
            }
        }
    }
}

