package de.esolutions.connectedradioclient.handlers.media

import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.StationLogo
import de.esolutions.connectedradioclient.data.backend.lookup.result.DataObject
import de.esolutions.connectedradioclient.data.backend.lookup.result.ETagResult
import de.esolutions.connectedradioclient.data.backend.lookup.result.ProviderObject
import de.esolutions.connectedradioclient.data.backend.lookup.result.StreamObject
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions.resetRequestConfig
import de.esolutions.connectedradioclient.helper.TestRadioStationDataCallback
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SendToMediaTest {

    @BeforeEach
    fun before() {
        resetRequestConfig()
    }

    @Test
    fun send_metadata() {
        val providerObject = ProviderObject(
            "Gracenote",
            "id"
        )
        val dataObject = DataObject(
            "shortName",
            "mediumName",
            "longName",
            "description",
            listOf(providerObject),
            listOf("fm:gcc.pi.frequency", "dab:gcc.eid.sid.scids", "hd:cc.tx.multicastId"),
            listOf(StreamObject("url", 0, "high"))
        )
        val callback = TestRadioStationDataCallback()
        val eTagResult = ETagResult("::metadata_etag::", "::bearer_etag::", "::stream_etag::")
        SendToMedia.metadata(null, "mcc", eTagResult, dataObject, callback)

        assertNotNull(callback.metadata)
        assertNotNull(callback.bearerList)
        assertNotNull(callback.streamList)

        assertEquals("::metadata_etag::", callback.metadataETag)
        assertEquals("::bearer_etag::", callback.bearerETag)
        assertEquals("::stream_etag::", callback.streamETag)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    fun send_no_bearers_or_stream_on_null() {
        val providerObject = ProviderObject(
            "Gracenote",
            "id"
        )
        val dataObject = DataObject(
            "shortName",
            "mediumName",
            "longName",
            "description",
            listOf(providerObject),
            null,
            null
        )
        val callback = TestRadioStationDataCallback()
        SendToMedia.metadata(null, "mcc", null, dataObject, callback)

        assertNotNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.METADATA
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    fun send_logo() {
        val stationLogo = StationLogo(ByteArray(5), "fileName", "PNG")
        val callback = TestRadioStationDataCallback()
        SendToMedia.logo(null, "::logo_etag::", stationLogo, callback, null)

        assertNotNull(callback.logo)
        assertNotNull(callback.completeStatus)
        assertNull(callback.expirationDate)

        assertEquals("::logo_etag::", callback.logoETag)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_metadata[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_logo_with_content_expiry() {
        val stationLogo = StationLogo(ByteArray(5), "fileName", "PNG")
        val callback = TestRadioStationDataCallback()
        SendToMedia.logo(null, "::logo_etag::", stationLogo, callback, 10)

        assertNotNull(callback.logo)
        assertNotNull(callback.completeStatus)
        assertNotNull(callback.expirationDate)

        assertEquals("::logo_etag::", callback.logoETag)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_delete_logo[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_delete_logo() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.deleteLogo(null, callback, null)

        assertNull(callback.logo)
        assertNull(callback.sLogo)
        assertNotNull(callback.completeStatus)
        assertNull(callback.expirationDate)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_delete_logo_with_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_delete_logo_with_content_expiry() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.deleteLogo(null, callback, 10)

        assertNull(callback.logo)
        assertNull(callback.sLogo)
        assertNotNull(callback.completeStatus)
        assertNotNull(callback.expirationDate)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_no_content[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-255771,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_no_content() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.noContent(null, callback, null)

        assertNull(callback.metadata)
        assertNull(callback.logo)
        assertNull(callback.sLogo)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertNull(callback.completeStatus!!.retryDelaySeconds)
        assertNull(callback.expirationDate)
        val expectedCalledFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_no_content_with_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_no_content_with_content_expiry() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.noContent(null, callback, 10)

        assertNull(callback.metadata)
        assertNull(callback.logo)
        assertNull(callback.sLogo)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertNull(callback.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback.expirationDate)
        val expectedCalledFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_no_content_for_all[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-255771,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_no_content_for_all() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)
        SendToMedia.noContentForAll(map, null, null)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE

        assertNull(callback1.metadata)
        assertNull(callback1.logo)
        assertNull(callback1.sLogo)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.NONE, callback1.completeStatus!!.retry)
        assertNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.logo)
        assertNull(callback2.sLogo)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.NONE, callback2.completeStatus!!.retry)
        assertNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName("send_no_content_for_all_with_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_no_content_for_all_with_content_expiry() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)
        SendToMedia.noContentForAll(map, null, 10)

        val expectedCalledFunctions =
            TestRadioStationDataCallback.METADATA or
                TestRadioStationDataCallback.BEARERS or
                TestRadioStationDataCallback.STREAMS or
                TestRadioStationDataCallback.LOGO or
                TestRadioStationDataCallback.SLOGO or
                TestRadioStationDataCallback.COMPLETE

        assertNull(callback1.metadata)
        assertNull(callback1.logo)
        assertNull(callback1.sLogo)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.NONE, callback1.completeStatus!!.retry)
        assertNotNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.logo)
        assertNull(callback2.sLogo)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.NONE, callback2.completeStatus!!.retry)
        assertNotNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName("send_not_modified[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-255771,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_not_modified() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.notModified(null, callback, null)
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertNull(callback.completeStatus!!.retryDelaySeconds)
        assertNull(callback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_not_modified_with_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_not_modified_with_content_expiry() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.notModified(null, callback, 10)
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertNull(callback.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_not_modified_for_all[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-255771,HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_not_modified_for_all() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)
        SendToMedia.notModifiedForAll(map, null, null)

        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE

        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.NONE, callback1.completeStatus!!.retry)
        assertNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.NONE, callback2.completeStatus!!.retry)
        assertNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName("send_not_modified_for_all_with_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_not_modified_for_all_with_content_expiry() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)
        SendToMedia.notModifiedForAll(map, null, 10)

        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE

        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.NONE, callback1.completeStatus!!.retry)
        assertNotNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.NONE, callback2.completeStatus!!.retry)
        assertNotNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    fun send_error() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.error(null, RetryBehavior.EXPONENTIAL, callback)
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.EXPONENTIAL, callback.completeStatus!!.retry)
        assertEquals(30, callback.completeStatus!!.retryDelaySeconds)
        assertNull(callback.expirationDate)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    fun send_error_for_all() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)
        SendToMedia.errorForAll(RetryBehavior.EXPONENTIAL, map, null)

        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE

        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.EXPONENTIAL, callback1.completeStatus!!.retry)
        assertNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.EXPONENTIAL, callback2.completeStatus!!.retry)
        assertNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName("send_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_retry_after() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.retryAfter(null, callback, 42L, null)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback.completeStatus!!.retry)
        assertEquals(42L, callback.completeStatus!!.retryDelaySeconds)
        assertNull(callback.expirationDate)
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_retry_after_with_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_retry_after_with_content_expiry() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.retryAfter(null, callback, 42L, 10)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback.completeStatus!!.retry)
        assertEquals(42L, callback.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback.expirationDate)
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_retry_after_for_all[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_retry_after_for_all() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)
        SendToMedia.retryAfterForAll(map, null, 42L, null)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback1.completeStatus!!.retry)
        assertEquals(42L, callback1.completeStatus!!.retryDelaySeconds)
        assertNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)
        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback2.completeStatus!!.retry)
        assertEquals(42L, callback2.completeStatus!!.retryDelaySeconds)
        assertNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName("send_retry_after_for_all_with_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_retry_after_for_all_with_content_expiry() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)
        SendToMedia.retryAfterForAll(map, null, 42L, 10)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback1.completeStatus!!.retry)
        assertEquals(42L, callback1.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)
        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback2.completeStatus!!.retry)
        assertEquals(42L, callback2.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName("send_retry_after_or_backend_preferences_no_retry_after[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_retry_after_or_backend_preferences_no_retry_after() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.retryAfterOrBackendPreferences(null, callback, null, RetryBehavior.NONE, null)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertEquals(30, callback.completeStatus!!.retryDelaySeconds)
        assertNull(callback.expirationDate)
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_retry_after_or_backend_preferences_no_content_expiry[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_retry_after_or_backend_preferences_no_content_expiry() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.retryAfterOrBackendPreferences(null, callback, 42, RetryBehavior.NONE, null)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback.completeStatus!!.retry)
        assertEquals(42, callback.completeStatus!!.retryDelaySeconds)
        assertNull(callback.expirationDate)
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName("send_retry_after_or_backend_preferences[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]")
    fun send_retry_after_or_backend_preferences() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.retryAfterOrBackendPreferences(null, callback, 42, RetryBehavior.NONE, 10)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback.completeStatus!!.retry)
        assertEquals(42, callback.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback.expirationDate)
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName(
        "send_retry_after_or_backend_preferences_no_retry_after_with_content_expiry" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun send_retry_after_or_backend_preferences_no_retry_after_with_content_expiry() {
        val callback = TestRadioStationDataCallback()
        SendToMedia.retryAfterOrBackendPreferences(null, callback, null, RetryBehavior.NONE, 10)
        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertEquals(30, callback.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback.expirationDate)
        assertEquals(expectedCalledFunctions, callback.calledFunctions)
    }

    @Test
    @DisplayName(
        "send_retry_after_or_backend_preferences_for_all_no_retry_after" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun send_retry_after_or_backend_preferences_for_all_no_retry_after() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)

        SendToMedia.retryAfterOrBackendPreferencesForAll(map, null, null, RetryBehavior.NONE, null)

        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.NONE, callback1.completeStatus!!.retry)
        assertEquals(30, callback1.completeStatus!!.retryDelaySeconds)
        assertNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.NONE, callback2.completeStatus!!.retry)
        assertEquals(30, callback2.completeStatus!!.retryDelaySeconds)
        assertNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName(
        "send_retry_after_or_backend_preferences_for_all" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun send_retry_after_or_backend_preferences_for_all() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)

        SendToMedia.retryAfterOrBackendPreferencesForAll(map, null, 42, RetryBehavior.NONE, 10)

        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback1.completeStatus!!.retry)
        assertEquals(42, callback1.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback2.completeStatus!!.retry)
        assertEquals(42, callback2.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName(
        "send_retry_after_or_backend_preferences_for_all_with_content_expiry" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun send_retry_after_or_backend_preferences_for_all_with_content_expiry() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)

        SendToMedia.retryAfterOrBackendPreferencesForAll(map, null, null, RetryBehavior.NONE, 10)

        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.NONE, callback1.completeStatus!!.retry)
        assertEquals(30, callback1.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.NONE, callback2.completeStatus!!.retry)
        assertEquals(30, callback2.completeStatus!!.retryDelaySeconds)
        assertNotNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }

    @Test
    @DisplayName(
        "send_retry_after_or_backend_preferences_for_all_with_retry_after" +
            "[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-291249#]"
    )
    fun send_retry_after_or_backend_preferences_for_all_with_retry_after() {
        val callback1 = TestRadioStationDataCallback()
        val callback2 = TestRadioStationDataCallback()
        val bearer = HdBearer("tx", "cc", "multicastId")
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject1 = MediaRequestObject(bearer, eTags, callback1)
        val mediaRequestObject2 = MediaRequestObject(bearer, eTags, callback2)
        val map = mapOf("1" to mediaRequestObject1, "2" to mediaRequestObject2)

        SendToMedia.retryAfterOrBackendPreferencesForAll(map, null, 42, RetryBehavior.NONE, null)

        val expectedCalledFunctions = TestRadioStationDataCallback.COMPLETE
        assertNull(callback1.metadata)
        assertNull(callback1.bearerList)
        assertNull(callback1.streamList)
        assertNotNull(callback1.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback1.completeStatus!!.retry)
        assertEquals(42, callback1.completeStatus!!.retryDelaySeconds)
        assertNull(callback1.expirationDate)
        assertEquals(expectedCalledFunctions, callback1.calledFunctions)

        assertNull(callback2.metadata)
        assertNull(callback2.bearerList)
        assertNull(callback2.streamList)
        assertNotNull(callback2.completeStatus)
        assertEquals(RetryBehavior.DELAYED, callback2.completeStatus!!.retry)
        assertEquals(42, callback2.completeStatus!!.retryDelaySeconds)
        assertNull(callback2.expirationDate)
        assertEquals(expectedCalledFunctions, callback2.calledFunctions)
    }
}