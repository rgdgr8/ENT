package technology.cariad.entertainment.metadataservice.repository

import android.net.Network
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import technology.cariad.entertainment.metadataservice.common.util.FlowValue
import technology.cariad.entertainment.metadataservice.data.provider.OnlineState
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import technology.cariad.entertainment.metadataservice.sampledata.SampleStation
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RadioStationDataRepositoryTest {

    private val radioStationDataProviderMock: IRadioStationDataProvider = mockk(relaxed = true)
    private val radioStationDataStoreMock: IRadioStationDataStore = mockk(relaxed = true)
    private val timeProviderMock: ITimeProvider = mockk(relaxed = true)
    private val onlineStateProviderMock: IOnlineStateProvider = mockk(relaxed = true)

    private val networkMock = mockk<Network>()
    private val onlineStateMock = mockk<OnlineState>()

    private lateinit var radioStationDataRepository: RadioStationDataRepository

    @Before
    fun setup() {
        LoggerMock()

        mockkObject(RequestLogic)

        mockkConstructor(RadioStationRequest::class)
        every {
            anyConstructed<RadioStationRequest>().stationRequest(any(), any(), any())
        } returns flow {
            emit(FlowValue(SampleStation.STATION_ENTITY_1))
        }

        every { radioStationDataProviderMock.serviceId } returns ServiceIdentifier.CONNECTEDRADIO_V2

        radioStationDataRepository =
            RadioStationDataRepository(
                radioStationDataProviderMock,
                radioStationDataStoreMock,
                timeProviderMock,
                onlineStateProviderMock
            )
    }

    @Test
    fun retrieveRadioStationData_shouldDoOfflineRequestIfOnlineNotAllowed(): Unit =
        runBlockingTest {

            val eTags = ETagsEntity()

            every { onlineStateProviderMock.onlineStateFlow(any(), any()) } returns flow {
                emit(onlineStateMock)
            }

            every { onlineStateMock.networkInfo } returns null

            val station =
                radioStationDataRepository.retrieveRadioStationData(SampleBearer.FM_BEARER_ENTITY_1, eTags)
                    .first()

            verify {
                anyConstructed<RadioStationRequest>().stationRequest(any(), eTags, false)
            }

            Assert.assertEquals(station, SampleStation.STATION_ENTITY_1)
        }

    @Test
    fun retrieveRadioStationData_shouldDoOnlineRequestIfOnlineAllowed(): Unit = runBlockingTest {
        val eTags = ETagsEntity()

        every { onlineStateProviderMock.onlineStateFlow(any(), any()) } returns flow {
            emit(onlineStateMock)
        }
        every { onlineStateMock.onlineLookupEnabled } returns true
        every { onlineStateMock.networkInfo } returns NetworkInfo(
            Apn.APN_CUSTOMER,
            networkMock,
            "interface"
        )

        val station =
            radioStationDataRepository.retrieveRadioStationData(
                SampleBearer.FM_BEARER_ENTITY_1,
                eTags
            )
                .first()

        verify {
            anyConstructed<RadioStationRequest>().stationRequest(any(), eTags, true)
        }

        Assert.assertEquals(station, SampleStation.STATION_ENTITY_1)
    }

    @Test
    fun retrieveRadioStationData_shouldDoNothingUntilLookupAllowed(): Unit = runBlockingTest {
        val eTags = ETagsEntity()

        every { onlineStateProviderMock.onlineStateFlow(any(), any()) } returns flowOf(
            null
        )

        val stations = radioStationDataRepository.retrieveRadioStationData(
            SampleBearer.FM_BEARER_ENTITY_1,
            eTags
        ).toList()

        verify {
            anyConstructed<RadioStationRequest>().stationRequest(any(), any(), any()) wasNot Called
        }

        Assert.assertTrue(stations.isEmpty())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

