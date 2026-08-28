package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.repository.SongInfoRepository
import technology.cariad.entertainment.metadataservice.sampledata.SampleSongInfo.Companion.SONG_INFO_ENTITY_2
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class RetrieveSongInfoTest {

    private lateinit var useCase: RetrieveSongInfo
    private val songInfoRepository: SongInfoRepository = mockk()

    @Test
    fun songInfoByFingerprinting_shouldReceiveSongInfoViaRepository() = runBlockingTest {

        useCase = RetrieveSongInfo(songInfoRepository)
        coEvery { songInfoRepository.retrieveSongInfo(any()) } returns flowOf(Success(SONG_INFO_ENTITY_2))
        useCase.songInfoByFingerprinting()

        coVerify {
            songInfoRepository.retrieveSongInfo(false)
        }
    }
}
