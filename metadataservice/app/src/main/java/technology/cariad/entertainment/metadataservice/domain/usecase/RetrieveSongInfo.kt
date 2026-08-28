package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.domain.ISongInfoRepository
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving song information.
 */
class RetrieveSongInfo(private val songInfoRepository: ISongInfoRepository) {

    /**
     * Provides song information.
     * @return a flow of [SongInfoEntity] including all known information about the song
     */
    fun songInfoByFingerprinting(): Flow<Result<SongInfoEntity>> {
        return songInfoRepository.retrieveSongInfo(false)
    }
}
