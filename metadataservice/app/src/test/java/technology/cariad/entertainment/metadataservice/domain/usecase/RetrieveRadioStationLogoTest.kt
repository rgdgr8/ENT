package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.repository.RadioStationDataRepository
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import technology.cariad.entertainment.metadataservice.sampledata.SampleStation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class RetrieveRadioStationLogoTest {

    private lateinit var useCase: RetrieveRadioStationLogo
    private val radioStationRepository: RadioStationDataRepository = mockk()

    @Test
    fun logoByBearer_shouldReceiveRadioStationLogo() = runBlockingTest {
        val eTags = ETagsEntity()

        useCase = RetrieveRadioStationLogo(radioStationRepository)
        coEvery { radioStationRepository.retrieveRadioStationData(any(), any()) } returns flowOf(
            SampleStation.STATION_ENTITY_1
        )
        useCase.logoByBearer(bearer = SampleBearer.DAB_BEARER_ENTITY_1, eTags)

        coVerify {
            radioStationRepository.retrieveRadioStationData(any(), eTags)
        }
    }
}
