package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationMetadataDao
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity

class MetadataStore(
    private val stationMetadataDao: StationMetadataDao,
    private val eTagsDao: ETagsDao
) {

    internal suspend fun deleteByStationId(stationId: Long) {
        eTagsDao.updateMetadataETag(stationId, "")
        stationMetadataDao.deleteByStationId(stationId)
    }

    internal suspend fun updateMetadata(
        stationId: Long,
        metadata: StationMetadataEntity,
    ) {
        val metadataId = stationMetadataDao.findIdByStationId(stationId) ?: 0
        val metadataEntry = stationMetadataDao.createStationMetadataEntry(
            metadata = metadata,
            stationId = stationId,
            metadataId = metadataId
        )
        stationMetadataDao.upsert(metadataEntry)
        eTagsDao.updateMetadataETag(stationId, metadata.etag ?: "")
    }
}

