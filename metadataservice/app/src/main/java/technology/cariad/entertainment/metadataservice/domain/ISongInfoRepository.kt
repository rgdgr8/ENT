package technology.cariad.entertainment.metadataservice.domain

import technology.cariad.entertainment.metadataservice.common.util.Result
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import kotlinx.coroutines.flow.Flow

interface ISongInfoRepository {

    /**
     * @param replay Indicates if the current result should be replayed or not. Note that the
     *               current result might be invalid after a source change or skip.
     * @exception [technology.cariad.entertainment.metadataservice.common.util.ServiceDisabledException] is
     * thrown is a service entry is missing or a disable reason exists
     * @exception [technology.cariad.entertainment.metadataservice.common.util.MissingPermissionException] is
     * thrown if [android.permission.RECORD_AUDIO] is missing
     */
    fun retrieveSongInfo(replay: Boolean): Flow<Result<SongInfoEntity>>
}
