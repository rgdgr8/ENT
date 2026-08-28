package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.util.FlowValue
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataProvider
import technology.cariad.entertainment.metadataservice.domain.IRadioStationDataStore
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.StationPart
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import technology.cariad.entertainment.metadataservice.sampledata.SampleStation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class RadioStationRequestTest {

    private val radioStationDataProvider: IRadioStationDataProvider = mockk(relaxed = true)
    private val radioStationDataStore: IRadioStationDataStore = mockk(relaxed = true)
    private val timeProvider: ITimeProvider = mockk(relaxed = true)

    private lateinit var radioStationRequest: RadioStationRequest

    private val eTags = ETagsEntity()

    @Before
    fun setup() {

        LoggerMock()

        mockkObject(RequestLogic)

        every { radioStationDataProvider.serviceId } returns ServiceIdentifier.CONNECTEDRADIO_V2

        radioStationRequest =
            RadioStationRequest(
                radioStationDataProvider,
                radioStationDataStore,
                timeProvider
            )
    }

    @Test
    fun stationRequest_shouldOnlyEmitCachedStationIfOnlineLookupDisabled() =
        runBlockingTest {
            val station = SampleStation.STATION_ENTITY_3

            coEvery { radioStationDataStore.getDataForBearer(any()) } returns station

            val expected = listOf(FlowValue(station))

            val result =
                radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, false)
                    .toList()

            Assert.assertEquals(expected, result)

            coVerifyAll {
                radioStationDataStore.getDataForBearer(any())
            }
        }

    @Test
    fun stationRequest_shouldEmitNothingIfNoStationCachedAndOnlineLookupDisabled() =
        runBlockingTest {
            coEvery { radioStationDataStore.getDataForBearer(any()) } returns null

            val expected = listOf<StationEntity>()

            val result =
                radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, false)
                    .toList()

            Assert.assertEquals(expected, result)

            coVerifyAll {
                radioStationDataStore.getDataForBearer(any())
                radioStationDataStore.updateLifeCycleData(any(), any())
            }
        }

    @Test
    fun stationRequest_shouldOnlyEmitCachedStationIfUpToDateAndOnlineLookupEnabled() =
        runBlockingTest {
            coEvery {
                radioStationDataStore.getDataForBearer(any())
            } returns SampleStation.STATION_ENTITY_1
            every { timeProvider.secondsSinceEpoch() } returns 0
            every { RequestLogic.updateRequired(any(), any()) } returns false

            val expected = listOf(FlowValue(SampleStation.STATION_ENTITY_1))

            val result =
                radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
                    .toList()

            Assert.assertEquals(expected, result)

            coVerify {
                radioStationDataStore.getDataForBearer(any())
            }

            coVerify(inverse = true) {
                radioStationDataProvider.getDataForBearer(any(), any())
            }
        }

    @Test
    fun stationRequest_shouldOnlyEmitCachedStationIfUpdateNotAllowed() = runBlockingTest {

        val cachedStation = SampleStation.STATION_ENTITY_1
        cachedStation.lifeCycleData!!.retryAfter = null

        coEvery { radioStationDataStore.getDataForBearer(any()) } returns cachedStation
        every { timeProvider.secondsSinceEpoch() } returns 0
        every { RequestLogic.updateRequired(any(), any()) } returns true
        every { RequestLogic.updatedAllowed(any(), any()) } returns false

        val expected = listOf(FlowValue(SampleStation.STATION_ENTITY_1.also {
            it.lifeCycleData!!.retryAfter = null
        }))

        val result =
            radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
                .toList()

        Assert.assertEquals(expected, result)

        coVerify {
            radioStationDataStore.getDataForBearer(any())
        }

        coVerify(inverse = true) {
            radioStationDataProvider.getDataForBearer(any(), any())
        }
    }

    @Test
    fun stationRequest_shouldOnlyEmitCachedStationIfRetryDelayLong() = runBlockingTest {

        val cachedStation = SampleStation.STATION_ENTITY_1
        cachedStation.lifeCycleData!!.retryAfter = 7 * 24 * 60 * 60

        coEvery { radioStationDataStore.getDataForBearer(any()) } returns cachedStation
        every { timeProvider.secondsSinceEpoch() } returns 0
        every { RequestLogic.updateRequired(any(), any()) } returns true
        every { RequestLogic.updatedAllowed(any(), any()) } returns false

        val expected = listOf(FlowValue(cachedStation))

        val result =
            radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
                .toList()

        Assert.assertEquals(expected, result)

        coVerify {
            radioStationDataStore.getDataForBearer(any())
        }

        coVerify(inverse = true) {
            radioStationDataProvider.getDataForBearer(any(), any())
        }
    }

    @Test
    fun stationRequest_shouldDelayStationDataUpdateIfRetryDelayShort() = runBlockingTest {

        val cachedStation = SampleStation.STATION_ENTITY_1
        cachedStation.lifeCycleData!!.retryAfter = 30
        val onlineUpdate = listOf(StationPart.LIFE_CYCLE_DATA to StationEntity())

        coEvery { radioStationDataStore.getDataForBearer(any()) } returns cachedStation
        every { timeProvider.secondsSinceEpoch() } returns 0
        every { RequestLogic.updateRequired(any(), any()) } returns true
        every { RequestLogic.updatedAllowed(any(), any()) } returns false
        every { RequestLogic.retryDelay(any(), any(), any()) } returns LifeCycleDataEntity()
        coEvery { radioStationDataProvider.getDataForBearer(any(), any()) } returns onlineUpdate

        val expected = listOf(FlowValue(cachedStation))

        val result =
            radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
                .toList()

        Assert.assertEquals(expected, result)

        coVerify {
            radioStationDataStore.getDataForBearer(any())
        }
    }

    @Test
    fun stationRequest_shouldOnlyEmitCachedStationIfUpdateInvalid() = runBlockingTest {

        val cachedStation = SampleStation.STATION_ENTITY_1
        cachedStation.lifeCycleData!!.retryAfter = 30

        coEvery { radioStationDataStore.getDataForBearer(any()) } returns cachedStation
        every { timeProvider.secondsSinceEpoch() } returns 0
        every { RequestLogic.updateRequired(any(), any()) } returns true
        every { RequestLogic.updatedAllowed(any(), any()) } returns false
        every { RequestLogic.retryDelay(any(), any(), any()) } returns LifeCycleDataEntity()
        coEvery { radioStationDataProvider.getDataForBearer(any(), any()) } returns listOf()

        val expected = listOf(FlowValue(cachedStation))

        val result =
            radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
                .toList()

        Assert.assertEquals(expected, result)

        coVerify {
            radioStationDataStore.getDataForBearer(any())
        }
    }

    @Test
    fun stationRequest_shouldStoreOnlineUpdates() = runBlockingTest {

        val cachedStation = SampleStation.STATION_ENTITY_1
        cachedStation.lifeCycleData!!.retryAfter = 30

        val onlineUpdates = listOf(
            StationPart.BEARER to SampleStation.STATION_ENTITY_2.bearerList,
            StationPart.METADATA to SampleStation.STATION_ENTITY_2.metadata,
            StationPart.LOGO to SampleStation.STATION_ENTITY_2.logo,
            StationPart.LOGO_SMALL to SampleStation.STATION_ENTITY_2.logoSmall,
            StationPart.STREAM to SampleStation.STATION_ENTITY_2.streamList,
            StationPart.LIFE_CYCLE_DATA to SampleStation.STATION_ENTITY_2.lifeCycleData
        )

        coEvery { radioStationDataStore.getDataForBearer(any()) } returns cachedStation
        every { timeProvider.secondsSinceEpoch() } returns 0
        every { RequestLogic.updateRequired(any(), any()) } returns true
        every { RequestLogic.updatedAllowed(any(), any()) } returns false
        every { RequestLogic.retryDelay(any(), any(), any()) } returns LifeCycleDataEntity()
        coEvery {
            radioStationDataProvider.getDataForBearer(
                any(),
                any()
            )
        } returns onlineUpdates

        radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
            .toList()

        coVerifyAll {
            radioStationDataStore.getDataForBearer(any())
            radioStationDataStore.updateBearers(any(), any())
            radioStationDataStore.updateMetadata(any(), any())
            radioStationDataStore.updateLogo(any(), any())
            radioStationDataStore.updateLogo(any(), any())
            radioStationDataStore.updateStreams(any(), any())
            radioStationDataStore.updateLifeCycleData(any(), any())
        }
    }

    @Test
    fun stationRequest_shouldNotStoreOnlineUpdatesIfUnchanged() = runBlockingTest {

        val cachedStation = SampleStation.STATION_ENTITY_1
        cachedStation.lifeCycleData!!.retryAfter = 30

        val onlineStation = SampleStation.STATION_ENTITY_1

        val onlineUpdates = listOf(
            StationPart.BEARER to onlineStation.bearerList,
            StationPart.METADATA to onlineStation.metadata,
            StationPart.LOGO to onlineStation.logo,
            StationPart.LOGO_SMALL to onlineStation.logoSmall,
            StationPart.STREAM to onlineStation.streamList,
            StationPart.LIFE_CYCLE_DATA to onlineStation
        )

        coEvery { radioStationDataStore.getDataForBearer(any()) } returns cachedStation
        every { timeProvider.secondsSinceEpoch() } returns 0
        every { RequestLogic.updateRequired(any(), any()) } returns true
        every { RequestLogic.updatedAllowed(any(), any()) } returns false
        every { RequestLogic.retryDelay(any(), any(), any()) } returns LifeCycleDataEntity()
        coEvery {
            radioStationDataProvider.getDataForBearer(any(), any())
        } returns onlineUpdates

        radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
            .toList()

        coVerify(inverse = true) {
            radioStationDataStore.updateBearers(any(), any())
            radioStationDataStore.updateMetadata(any(), any())
            radioStationDataStore.updateStreams(any(), any())
        }
    }

    @Test
    fun stationRequest_shouldIgnoreInvalidStationPartTypes() = runBlockingTest {

        val cachedStation = SampleStation.STATION_ENTITY_1
        cachedStation.lifeCycleData!!.retryAfter = 30

        val onlineUpdates = listOf(
            StationPart.BEARER to Double,
            StationPart.METADATA to Double,
            StationPart.LOGO to Double,
            StationPart.LOGO_SMALL to Double,
            StationPart.STREAM to Double,
            StationPart.LIFE_CYCLE_DATA to Double
        )

        coEvery { radioStationDataStore.getDataForBearer(any()) } returns cachedStation
        every { timeProvider.secondsSinceEpoch() } returns 0
        every { RequestLogic.updateRequired(any(), any()) } returns true
        every { RequestLogic.updatedAllowed(any(), any()) } returns false
        every { RequestLogic.retryDelay(any(), any(), any()) } returns LifeCycleDataEntity()
        coEvery {
            radioStationDataProvider.getDataForBearer(any(), any())
        } returns onlineUpdates

        radioStationRequest.stationRequest(SampleBearer.FM_BEARER_ENTITY_1, eTags, true)
            .toList()

        coVerify(inverse = true) {
            radioStationDataStore.updateBearers(any(), any())
            radioStationDataStore.updateMetadata(any(), any())
            radioStationDataStore.updateStreams(any(), any())
            radioStationDataStore.updateLogo(any(), any())
            radioStationDataStore.updateLifeCycleData(any(), any())
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

