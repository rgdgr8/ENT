package technology.cariad.entertainment.metadataservice.data.provider

import de.esolutions.connectedradioclient.data.CompleteStatus
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.handlers.media.ConRadClient
import de.esolutions.connectedradioclient.handlers.media.IRadioStationDataCallback
import de.esolutions.magicadapter.stapi.AuthenticatorConstants
import de.esolutions.magicadapter.stapi.Platform
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import technology.cariad.entertainment.metadataservice.common.servicelist.HttpOperation
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.domain.StationPart
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RadioStationDataProviderTest {

    private val conRadClientMock = mockk<ConRadClient>()
    private lateinit var radioStationDataProvider: RadioStationDataProvider

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private val serviceId = ServiceIdentifier.HYBRIDRADIODATA_V3
    private val bearerMock = mockk<FmRdsBearerEntity>()
    private val eTagsEntityMock = mockk<ETagsEntity>()
    private val networkInfo = NetworkInfo(Apn.APN_VENDOR, mockk(), "eth0")
    private val serviceListEntryMock = mockk<ServiceListEntry>()

    @Before
    fun setup() {

        every { bearerMock.toString() } returns "bearer"
        every { bearerMock.mcc } returns "mcc"
        every { bearerMock.pi } returns "pi"
        every { bearerMock.ecc } returns "ecc"
        every { bearerMock.frequencyKhz } returns "frequency"

        every { eTagsEntityMock.metadataETag } returns "meta"
        every { eTagsEntityMock.bearerListETag } returns "bearers"
        every { eTagsEntityMock.streamsETag } returns "streams"
        every { eTagsEntityMock.logoETag } returns "logo"
        every { eTagsEntityMock.logoSmallETag } returns "slogo"

        radioStationDataProvider = RadioStationDataProvider(serviceId, Platform.OI, conRadClientMock)
    }

    @Test
    fun `getDataForBearer should collect all station parts`() = testScope.runBlockingTest {
        val callbackSlot = slot<IRadioStationDataCallback>()

        every {
            conRadClientMock.getRadioStationData(any(), any(), capture(callbackSlot))
        } answers {
            callbackSlot.captured.onBearers("etag1", listOf())
            callbackSlot.captured.onMetadata("etag2", null)
            callbackSlot.captured.onStreams("etag3", listOf())
            callbackSlot.captured.onLogo("etag4", null)
            callbackSlot.captured.onSLogo("etag5", null)

            val status = CompleteStatus(RetryBehavior.NONE, 10)
            callbackSlot.captured.onComplete(status, 123456789)
        }

        val result = radioStationDataProvider.getDataForBearer(bearerMock, eTagsEntityMock)

        assert(result.isNotEmpty())
        assert(result.any { it.first == StationPart.LIFE_CYCLE_DATA })
    }

    @Test
    fun `selectNetwork should delegate to conRadClient`() {
        every { conRadClientMock.setNetwork(any()) } just Runs

        radioStationDataProvider.selectNetwork(networkInfo)

        verify { conRadClientMock.setNetwork(networkInfo.network) }
    }

    @Test
    fun `selectNetwork should clear network if null`() {
        every { conRadClientMock.setNetwork(any()) } just Runs

        radioStationDataProvider.selectNetwork(null)

        verify { conRadClientMock.setNetwork(null) }
    }

    @Test
    fun `setServiceConfig should apply config and call conRadClient`() {
        val operationMock = mockk<HttpOperation>()
        every { operationMock.url } returns "http://example.com"
        every { serviceListEntryMock.defaultHttpOperation } returns operationMock

        every { conRadClientMock.setConfig(any(), any()) } just Runs

        radioStationDataProvider.setServiceConfig(serviceListEntryMock)

        verify { conRadClientMock.setConfig(any(), any()) }
    }

    @Test
    fun `getDataForBearer uses correct constants for Platform HCP3`() = testScope.runBlockingTest {
        radioStationDataProvider = RadioStationDataProvider(serviceId, Platform.HCP3, conRadClientMock)

        var usedServiceId: String? = null
        var usedOperationTypeId: String? = null
        var usedOptionServiceId: String? = null
        var usedOptionOperationId: String? = null

        every {
            conRadClientMock.getRadioStationData(any(), any(), any())
        } answers {
            usedServiceId = AuthenticatorConstants.BUNDLE_OPTIONS_SERVICEID
            usedOperationTypeId = AuthenticatorConstants.BUNDLE_OPTIONS_OPERATIONID_DEFAULT
            usedOptionServiceId = AuthenticatorConstants.BUNDLE_OPTIONS_SERVICEID
            usedOptionOperationId = AuthenticatorConstants.BUNDLE_OPTIONS_OPERATIONID

            val callback = thirdArg<IRadioStationDataCallback>()
            callback.onComplete(CompleteStatus(RetryBehavior.NONE, 0), 0L)
        }

        radioStationDataProvider.getDataForBearer(bearerMock, eTagsEntityMock)

        assertEquals("test_svc_id", usedServiceId)
        assertEquals("test_default_op", usedOperationTypeId)
        assertEquals("test_svc_id", usedOptionServiceId)
        assertEquals("test_op_id", usedOptionOperationId)
    }


    @After
    fun tearDown() {
        unmockkAll()
    }
}

