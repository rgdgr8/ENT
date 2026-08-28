package de.esolutions.connectedradioclient.handlers.media

import android.content.Context
import de.esolutions.connectedradioclient.data.AccountManagerConfig
import de.esolutions.connectedradioclient.data.CompleteStatus
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.EndpointConfig
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.StationMetadata
import de.esolutions.connectedradioclient.data.bearer.DabBearer
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.handlers.backend.executor.ExecutorWorker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

/**
 * To use this test, you have to insert your token and comment out the disabled
 * annotation above the test.
 *
 * This test asks the connectedradio-v2.tui.car-cloud.org backend for bearer data.
 * You can see the result in the test logs.
 * The test returns success if the process has been completed, even if there have been
 * errors that get caught by conrad. The return is not checked automatically as this real data
 * can change or there could be errors in the backend.
 *
 * Test logbook:
 * - nifi7801 2021-10-21: OK - Test behaves as expected, with the current state (44996115).
 * - nifi7801 2021-11-25: OK - v45049751
 * - nifi7801 2022-12-12: OK - v45781468
 * - nifi7801 2023-02-27: OK - v46353258 HCPG-256035
 * - nifi7801 2023-08-21: OK - v48605769 HCPG-264255
 */
class ConRadClientIntegrationTest {

    private lateinit var conRadClient: ConRadClient

    private val antenneBayernBearer = DabBearer("262", "e0", "10a5", "d318", "0")
    private val noContentBearer = FmBearer("262", "d318", "10570")

    private val noETags = ETags(null, null, null, null, null)

    /**
     * Workaround to use Mockito.any() with kotlin.
     * https://stackoverflow.com/a/61746568
     */
    private fun <T> any(type: Class<T>): T = Mockito.any(type)

    @BeforeEach
    fun init() {
        conRadClient = ConRadClient(Mockito.mock(Context::class.java), null)
        conRadClient.setConfig(
            AccountManagerConfig(
                "serviceId",
                "accountType",
                "accountName",
                "optionServiceId",
                "optionOperationId"
            ),
            EndpointConfig("https://connectedradio-v2.tui.car-cloud.org", "DEFAULT")
        )
        conRadClient.setToken("::token::") // Add your token here
        ExecutorWorker.start()
    }

    @Test
    @Disabled("Need Backend and Token") // Comment out this line to use this test.
    fun receive_data_or_not_modified_from_backend() {
        val okCallback = Mockito.mock(IRadioStationDataCallback::class.java)
        conRadClient.getRadioStationData(antenneBayernBearer, noETags, okCallback)

        Mockito.verify(okCallback, Mockito.timeout(10000L)).onComplete(
            any(CompleteStatus::class.java),
            ArgumentMatchers.anyLong()
        )

        val metadataETag = ArgumentCaptor.forClass(String::class.java)
        val bearersETag = ArgumentCaptor.forClass(String::class.java)
        val streamsETag = ArgumentCaptor.forClass(String::class.java)
        val logoETag = ArgumentCaptor.forClass(String::class.java)

        Mockito.verify(okCallback)
            .onMetadata(metadataETag.capture(), any(StationMetadata::class.java))
        Mockito.verify(okCallback)
            .onBearers(bearersETag.capture(), Mockito.anyList())
        Mockito.verify(okCallback)
            .onStreams(streamsETag.capture(), Mockito.anyList())
        Mockito.verify(okCallback)
            .onLogo(logoETag.capture(), any(StationLogo::class.java))

        val antenneBayernETags = ETags(
            metadataETag.value, bearersETag.value, streamsETag.value, logoETag.value, null
        )
        val notModifiedCallback = Mockito.mock(IRadioStationDataCallback::class.java)
        conRadClient.getRadioStationData(
            antenneBayernBearer,
            antenneBayernETags,
            notModifiedCallback
        )

        Mockito.verify(notModifiedCallback, Mockito.timeout(10000L)).onComplete(
            any(CompleteStatus::class.java),
            ArgumentMatchers.anyLong()
        )

        Mockito.verify(notModifiedCallback, Mockito.never())
            .onMetadata(metadataETag.capture(), any(StationMetadata::class.java))
        Mockito.verify(notModifiedCallback, Mockito.never())
            .onBearers(bearersETag.capture(), Mockito.anyList())
        Mockito.verify(notModifiedCallback, Mockito.never())
            .onStreams(streamsETag.capture(), Mockito.anyList())
        Mockito.verify(notModifiedCallback, Mockito.never())
            .onLogo(logoETag.capture(), any(StationLogo::class.java))
    }

    @Test
    @Disabled("Need Backend and Token") // Comment out this line to use this test.
    fun receive_no_content_from_backend_if_bearer_does_not_exist() {
        val noContentCallback = Mockito.mock(IRadioStationDataCallback::class.java)
        conRadClient.getRadioStationData(noContentBearer, noETags, noContentCallback)

        Mockito.verify(noContentCallback, Mockito.timeout(10000L)).onComplete(
            any(CompleteStatus::class.java),
            ArgumentMatchers.anyLong()
        )

        Mockito.verify(noContentCallback).onMetadata(Mockito.isNull(), Mockito.isNull())
        Mockito.verify(noContentCallback).onBearers(Mockito.isNull(), Mockito.isNull())
        Mockito.verify(noContentCallback).onStreams(Mockito.isNull(), Mockito.isNull())
        Mockito.verify(noContentCallback).onLogo(Mockito.isNull(), Mockito.isNull())
    }

}