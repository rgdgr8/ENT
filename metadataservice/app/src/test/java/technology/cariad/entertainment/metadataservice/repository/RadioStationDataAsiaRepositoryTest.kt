package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.position.PositionInformation
import technology.cariad.entertainment.metadataservice.common.position.PositionProvider
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.data.provider.OnlineState
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataAsiaRepository
import technology.cariad.entertainment.metadataservice.domain.entity.StationAsiaEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearerAsia
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationAsia
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RadioStationDataAsiaRepositoryTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val dataProviderMock = mockk<IRadioStationDataAsiaProvider>(relaxed = true)
    private val imageStoreMock = mockk<IImageDataStore>()
    private val onlineStateProviderMock = mockk<IOnlineStateProvider>(relaxed = true)
    private val positionProviderMock = mockk<PositionProvider>()

    private val networkInfoMock = mockk<NetworkInfo>()
    private val onlineStateMock = mockk<OnlineState>()
    private val serviceListEntryMock = mockk<ServiceListEntry>()

    private lateinit var radioStationArtRepository: IRadioStationDataAsiaRepository

    @Before
    fun setup() {
        LoggerMock()

        every { positionProviderMock.positionFlow() } returns MutableStateFlow(Success(null))
        every { dataProviderMock.serviceId } returns ServiceIdentifier.HYBRIDRADIODATA_V3

        radioStationArtRepository = RadioStationDataAsiaRepository(
            dataProviderMock,
            imageStoreMock,
            onlineStateProviderMock,
            positionProviderMock
        )
    }

    @Test
    fun retrieveRadioStationData_shouldReturnStationForOfflineLookupIfCached() =
        testScope.runBlockingTest {
            every { onlineStateProviderMock.onlineStateFlow(any(), any()) } returns flow {
                emit(onlineStateMock)
            }
            every { onlineStateMock.onlineLookupEnabled } returns true
            every { onlineStateMock.networkInfo } returns networkInfoMock
            every { onlineStateMock.serviceListEntry } returns serviceListEntryMock
            every { onlineStateMock.countryOfSales } returns "DE"

            coEvery { positionProviderMock.lastPosition() } returns Success(
                PositionInformation(
                    0.0,
                    0.0
                )
            )
            coEvery {
                dataProviderMock.getDataForBearer(any(), any())
            } returns SampleStationAsia.STATION_ASIA_ENTITY_1
            every { imageStoreMock.saveImage(any(), any()) } returns "saved_uri"

            val station = radioStationArtRepository.retrieveRadioStationData(
                SampleBearerAsia.BEARER_ASIA_FM_ENTITY_1
            )

            Assert.assertNotEquals(StationAsiaEntity(), station)
        }

    @Test
    fun retrieveRadioStationData_shouldStoreImage() = testScope.runBlockingTest {
        every { onlineStateProviderMock.onlineStateFlow(any(), any()) } returns flow {
            emit(onlineStateMock)
        }
        every { onlineStateMock.onlineLookupEnabled } returns true
        every { onlineStateMock.networkInfo } returns networkInfoMock
        every { onlineStateMock.serviceListEntry } returns serviceListEntryMock
        every { onlineStateMock.countryOfSales } returns "DE"

        coEvery { positionProviderMock.lastPosition() } returns Success(
            PositionInformation(
                0.0,
                0.0
            )
        )
        coEvery { dataProviderMock.getDataForBearer(any(), any()) } returns
                SampleStationAsia.STATION_ASIA_ENTITY_1
        every { imageStoreMock.saveImage(any(), any()) } returns "saved_uri"

        val station = radioStationArtRepository.retrieveRadioStationData(
            SampleBearerAsia.BEARER_ASIA_FM_ENTITY_1
        )

        Assert.assertEquals("saved_uri", station.logo!!.uri)

        coVerify(exactly = 1) {
            imageStoreMock.saveImage(any(), any())
        }
    }

    @Test
    fun retrieveRadioStationData_shouldLoadImageIfAlreadyStored() = testScope.runBlockingTest {
        every { onlineStateProviderMock.onlineStateFlow(any(), any()) } returns flow {
            emit(onlineStateMock)
        }
        every { onlineStateMock.onlineLookupEnabled } returns true
        every { onlineStateMock.networkInfo } returns networkInfoMock
        every { onlineStateMock.serviceListEntry } returns serviceListEntryMock
        every { onlineStateMock.countryOfSales } returns "DE"

        coEvery { positionProviderMock.lastPosition() } returns Success(
            PositionInformation(
                0.0,
                0.0
            )
        )
        coEvery { dataProviderMock.getDataForBearer(any(), any()) } returns
                SampleStationAsia.STATION_ASIA_ENTITY_1
        every { imageStoreMock.saveImage(any(), any()) } returns null
        every { imageStoreMock.loadImageUri(any()) } returns "loaded_uri"

        val station = radioStationArtRepository.retrieveRadioStationData(
            SampleBearerAsia.BEARER_ASIA_FM_ENTITY_1
        )

        Assert.assertEquals("loaded_uri", station.logo!!.uri)

        coVerifyOrder {
            imageStoreMock.saveImage(any(), any())
            imageStoreMock.loadImageUri(any())
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

