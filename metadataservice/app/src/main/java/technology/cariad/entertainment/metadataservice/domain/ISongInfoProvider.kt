package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import kotlinx.coroutines.flow.Flow

interface ISongInfoProvider : IOnlineService {

    /**
     * Provides metadata about currently playing radio or media.
     * @return flow of [SongInfoEntity] and [ImageEntity] pairs
     */
    fun getSongInfo(): Flow<Pair<SongInfoEntity, ImageEntity>>
}
