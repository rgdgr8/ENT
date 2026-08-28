package technology.cariad.entertainment.metadataservice.data.provider

import android.net.Network
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.position.PositionInformation
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGracenoteService
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IRadioStationCallback
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearerAsia
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationAsia
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.UUID

class RadioStationDataAsiaProviderTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val gracenoteServiceMock = mockk<IGracenoteService>()
    private val networkMock = mockk<Network>()
    private val radioStationDataAsiaProvider =
        RadioStationDataAsiaProvider(ServiceIdentifier.HYBRIDRADIODATA_V3, gracenoteServiceMock)

    private val position = PositionInformation(48.765313613, 11.4245194)

    @Before
    fun setup() {
        LoggerMock()

        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns UUID.fromString(SampleStationAsia.IMAGE_NAME_UUID)
    }

    @Test
    fun getDataForBearer_shouldReturnStationForValidResult() = testScope.runBlockingTest {
        val slot = slot<IRadioStationCallback>()

        every {
            gracenoteServiceMock.requestRadioStationData(any(), any(), any(), capture(slot))
        } answers {
            slot.captured.onRadioStationData(
                SampleStationAsia.STATION_ASIA_NAME_1,
                SampleStationAsia.STATION_PROVIDER_ID_1,
                SampleStationAsia.STATION_ASIA_IMAGE_DATA_1
            )
        }

        val station = radioStationDataAsiaProvider.getDataForBearer(
            SampleBearerAsia.BEARER_ASIA_FM_ENTITY_1,
            position
        )

        Assert.assertEquals(SampleStationAsia.STATION_ASIA_ENTITY_1, station)
    }

    @Test
    fun getDataForBearer_shouldReturnEmptyStationForEmptyResult() = testScope.runBlockingTest {
        val slot = slot<IRadioStationCallback>()

        every {
            gracenoteServiceMock.requestRadioStationData(any(), any(), any(), capture(slot))
        } answers {
            slot.captured.onRadioStationData("", "", byteArrayOf())
        }

        val station = radioStationDataAsiaProvider.getDataForBearer(
            SampleBearerAsia.BEARER_ASIA_AM_ENTITY_1,
            position
        )

        Assert.assertEquals(SampleStationAsia.STATION_ASIA_ENTITY_EMPTY_1, station)
    }

    @Test
    fun selectNetwork_shouldSetInterfaceForRequestTypeIfNetworkAvailable() {
        val networkInfo = NetworkInfo(Apn.APN_VENDOR, networkMock, "eth42")

        every { gracenoteServiceMock.updateApnConfigEntry(any(), any()) } just Runs

        radioStationDataAsiaProvider.selectNetwork(networkInfo)

        verify {
            gracenoteServiceMock.updateApnConfigEntry(
                GnServiceRequestNr.RADIOSTATION,
                "eth42"
            )
        }
    }

    @Test
    fun selectNetwork_shouldClearInterfaceForRequestTypeIfNetworkUnavailable() {
        every { gracenoteServiceMock.updateApnConfigEntry(any(), any()) } just Runs

        radioStationDataAsiaProvider.selectNetwork(null)

        verify {
            gracenoteServiceMock.updateApnConfigEntry(
                GnServiceRequestNr.RADIOSTATION,
                null
            )
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

