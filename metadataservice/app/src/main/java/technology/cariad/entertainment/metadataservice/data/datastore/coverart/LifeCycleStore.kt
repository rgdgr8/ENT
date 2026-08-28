package technology.cariad.entertainment.metadataservice.data.datastore.coverart

import technology.cariad.entertainment.metadataservice.data.database.dao.LifeCycleDataDao
import technology.cariad.entertainment.metadataservice.data.database.entity.LifeCycleDataEntry
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity

class LifeCycleStore(private val lifeCycleDataDao: LifeCycleDataDao) {

    internal fun createLifeCycleDataEntry(
        lifeCycleDataId: Long = 0,
        lifeCycleDataEntity: LifeCycleDataEntity
    ): LifeCycleDataEntry {
        return lifeCycleDataDao.createLifeCycleDataEntry(lifeCycleDataId, lifeCycleDataEntity)
    }

    internal suspend fun insertLifeCycleData(lifeCycleDataEntry: LifeCycleDataEntry): Long {
        return lifeCycleDataDao.insert(lifeCycleDataEntry)
    }

    internal suspend fun updateLifeCycleData(lifeCycleDataEntry: LifeCycleDataEntry) {
        lifeCycleDataDao.update(lifeCycleDataEntry)
    }

    internal suspend fun deleteLifeCycleDataById(lifeCycleDataId: Long) {
        lifeCycleDataDao.deleteById(lifeCycleDataId)
    }
}

