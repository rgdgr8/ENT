package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.ETagsDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StreamDao
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity

class StreamStore(private val streamDao: StreamDao, private val eTagsDao: ETagsDao) {

    internal suspend fun deleteByStationId(stationId: Long): Int {
        eTagsDao.updateStreamsETag(stationId, "")
        return streamDao.deleteByStationId(stationId)
    }

    internal suspend fun insertStreams(stationId: Long, streamList: StationStreamListEntity) {
        val streamEntries = streamList.streams.mapIndexed { index, stream ->
            streamDao.createStreamEntry(stream, stationId, index)
        }
        streamDao.insert(streamEntries)
        eTagsDao.updateStreamsETag(stationId, streamList.etag ?: "")
    }

    internal suspend fun updateStreams(stationId: Long, streamList: StationStreamListEntity) {
        if (streamList.streams.isNotEmpty()) {
            streamDao.deleteByStationId(stationId)
            insertStreams(stationId, streamList)
        }
    }
}

