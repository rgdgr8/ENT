package technology.cariad.entertainment.metadataservice.domain.usecase

import technology.cariad.entertainment.metadataservice.repository.RadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearerAsia
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationAsia
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class RetrieveRadioStationAsiaTest {

    private val radioStationAsiaRepository = mockk<RadioStationDataAsiaRepository>()

    private val useCase = RetrieveRadioStationAsia(radioStationAsiaRepository)

    @Test
    fun logoByBearer_shouldReceiveRadioStationLogo() = runBlockingTest {

        coEvery { radioStationAsiaRepository.retrieveRadioStationData(any()) } returns
                SampleStationAsia.STATION_ASIA_ENTITY_1

        useCase.dataByBearer(SampleBearerAsia.BEARER_ASIA_FM_ENTITY_1)

        coVerify {
            radioStationAsiaRepository.retrieveRadioStationData(any())
        }
    }
}

