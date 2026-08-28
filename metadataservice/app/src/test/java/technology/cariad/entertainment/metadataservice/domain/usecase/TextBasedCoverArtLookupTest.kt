package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.repository.CoverArtRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class TextBasedCoverArtLookupTest {

    private lateinit var useCase: TextBasedCoverArtLookup
    private val coverArtRepository: CoverArtRepository = mockk()

    @Test
    fun coverArtImageByInfo_shouldReceiveAUriViaRepositoryWithoutTrack() = runBlockingTest {

        val coverArtRequestData = CoverArtRequestDataEntity(
            album = "Like a Prayer",
            artist = "Madonna",
            title = "",
            lifeCycleData = null
        )

        useCase = TextBasedCoverArtLookup(coverArtRepository)
        coEvery { coverArtRepository.retrieveCoverArtImage(any()) } returns null
        useCase.coverArtImageByInfo(coverArtRequestData)

        coVerify {
            coverArtRepository.retrieveCoverArtImage(any())
        }

        confirmVerified(coverArtRepository)
    }

    @Test
    fun coverArtImageByInfo_shouldReceiveAUriViaRepositoryWithoutAlbum() = runBlockingTest {

        val coverArtRequestData = CoverArtRequestDataEntity(
            album = "",
            artist = "Madonna",
            title = "Frozen",
            lifeCycleData = null
        )

        useCase = TextBasedCoverArtLookup(coverArtRepository)
        coEvery { coverArtRepository.retrieveCoverArtImage(any()) } returns null
        useCase.coverArtImageByInfo(coverArtRequestData)

        coVerify {
            coverArtRepository.retrieveCoverArtImage(any())
        }

        confirmVerified(coverArtRepository)
    }

    @Test
    fun coverArtImageByInfo_shouldReceiveAUriViaRepositoryWithOnlyAlbum() = runBlockingTest {

        val coverArtRequestData = CoverArtRequestDataEntity(
            album = "Like a Prayer",
            artist = "",
            title = "",
            lifeCycleData = null
        )

        useCase = TextBasedCoverArtLookup(coverArtRepository)
        coEvery { coverArtRepository.retrieveCoverArtImage(any()) } returns null
        useCase.coverArtImageByInfo(coverArtRequestData)

        coVerify {
            coverArtRepository.retrieveCoverArtImage(any())
        }

        confirmVerified(coverArtRepository)
    }

    @Test
    fun coverArtImageByInfo_shouldReceiveNullUriWithoutAlbumAndArtist() = runBlockingTest {

        val coverArtRequestData = CoverArtRequestDataEntity(
            album = "",
            artist = "",
            title = "Frozen",
            lifeCycleData = null
        )

        useCase = TextBasedCoverArtLookup(coverArtRepository)
        useCase.coverArtImageByInfo(coverArtRequestData)

        coVerify(inverse = true) {
            coverArtRepository.retrieveCoverArtImage(any())
        }

        confirmVerified(coverArtRepository)
    }

    @Test
    fun coverArtImageByInfo_shouldReceiveNullUriWithoutAlbumAndTrack() = runBlockingTest {

        val coverArtRequestData = CoverArtRequestDataEntity(
            album = "",
            artist = "Madonna",
            title = "",
            lifeCycleData = null
        )

        useCase = TextBasedCoverArtLookup(coverArtRepository)
        useCase.coverArtImageByInfo(coverArtRequestData)

        coVerify(inverse = true) {
            coverArtRepository.retrieveCoverArtImage(any())
        }

        confirmVerified(coverArtRepository)
    }
}
