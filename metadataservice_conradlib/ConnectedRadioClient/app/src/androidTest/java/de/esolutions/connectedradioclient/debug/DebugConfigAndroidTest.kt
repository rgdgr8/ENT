package de.esolutions.connectedradioclient.debug

import de.esolutions.connectedradioclient.data.DEBUG_STRING
import de.esolutions.connectedradioclient.data.ETags
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.data.enums.Origin
import de.esolutions.connectedradioclient.data.enums.RetryBehavior
import de.esolutions.connectedradioclient.data.enums.StreamQuality
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class DebugConfigAndroidTest {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    private lateinit var debugController: DebugController

    @BeforeEach
    fun init() {
        debugController = DebugController()
    }

    @Test
    fun it_returns_debug_data() {
        val bearer = HdBearer("tx", "cc", "multicastId")
        val debugConfig = DebugConfig()
        debugController.setDebugConfig(debugConfig)
        val callback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)
        debugController.getRadioStationData(mediaRequestObject)

        val metadata = callback.metadata
        assertNotNull(metadata)
        val providerIdentifier = metadata!!.providerIdentifiers
        assertEquals(Origin.RADIOPLAYER, providerIdentifier[0].origin)
        assertEquals(DEBUG_STRING, providerIdentifier[0].externalId)
        assertEquals("hd:cc.tx.multicastId - shortName", metadata.shortName)
        assertEquals("hd:cc.tx.multicastId - mediumName", metadata.mediumName)
        assertEquals("hd:cc.tx.multicastId - longName", metadata.longName)
        assertEquals("hd:cc.tx.multicastId - description", metadata.description)
        assertEquals(listOf("genre1", "genre2"), metadata.genres)

        val logo = callback.logo
        assertNotNull(logo)
        assertNotNull(logo!!.imageData)
        assertEquals("fd95708e817392230b86d4f3a458cef4", logo.fileName)
        assertEquals("WEBP", logo.fileExtension)

        val sLogo = callback.sLogo
        assertNotNull(sLogo)
        assertNotNull(sLogo!!.imageData)
        assertEquals("fd95708e817392230b86d4f3a458cef4", sLogo.fileName)
        assertEquals(sLogo.fileExtension, "PNG")

        val streams = callback.streamList
        assertNotNull(streams)
        assertEquals(streams!!.size, 2)
        val stream1 = streams[0]
        assertEquals("http://addrad.io/4WRD9v", stream1.url)
        assertEquals(0, stream1.offsetMillis)
        assertEquals(StreamQuality.LOW, stream1.streamQuality)
        val stream2 = streams[1]
        assertEquals("http://addrad.io/4WRDbj", stream2.url)
        assertEquals(0, stream2.offsetMillis)
        assertEquals(StreamQuality.HIGH, stream2.streamQuality)

        val bearers = callback.bearerList
        assertNotNull(bearers)
        assertEquals(bearers!!.size, 1)
        val hdBearer = bearers[0] as HdBearer
        assertEquals("tx", hdBearer.tx)
        assertEquals("cc", hdBearer.cc)
        assertEquals("multicastId", hdBearer.multicastId)

        val status = callback.completeStatus
        assertNotNull(status)
        assertEquals(RetryBehavior.NONE, status!!.retry)
        assertNull(status.retryDelaySeconds)
        assertNotNull(callback.expirationDate)
    }

    @Test
    fun it_returns_no_data() {
        val bearer = HdBearer("tx", "cc", "multicastId")
        val debugConfig = DebugConfig()
        debugConfig.sendSLogo = 0.0
        debugConfig.sendLogo = 0.0
        debugConfig.sendMetadata = 0.0
        debugConfig.sendWebStreams = 0.0
        debugConfig.sendBearerList = 0.0
        debugController.setDebugConfig(debugConfig)
        val callback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)
        debugController.getRadioStationData(mediaRequestObject)

        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNull(callback.logo)
        assertNull(callback.sLogo)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertNotNull(callback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, callback.calledFunctions)
    }

    @Test
    fun it_returns_only_an_error() {
        val bearer = HdBearer("tx", "cc", "multicastId")
        val debugConfig = DebugConfig()
        debugConfig.sendError = 1.0
        debugController.setDebugConfig(debugConfig)
        val callback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)
        debugController.getRadioStationData(mediaRequestObject)

        assertNull(callback.metadata)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNull(callback.logo)
        assertNull(callback.sLogo)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.ONCE, callback.completeStatus!!.retry)
        assertNull(callback.expirationDate)
        val expectedCallFunctions = TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, callback.calledFunctions)
    }

    @Test
    fun update_logo() {
        val bearer = HdBearer("tx", "cc", "multicastId")
        val debugConfig = DebugConfig()
        debugConfig.sendSLogo = 0.0
        debugController.setDebugConfig(debugConfig)
        val callback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)
        debugController.getRadioStationData(mediaRequestObject)

        debugController.getRadioStationData(mediaRequestObject)
        val logo1 = callback.logo
        assertNotNull(logo1)
        debugController.getRadioStationData(mediaRequestObject)
        val logo2 = callback.logo
        assertNotNull(logo2)
        assertNotEquals(logo1!!.imageData, logo2!!.imageData)
    }

    @Test
    fun callback_should_wait_min_20Seconds() {
        val bearer = HdBearer("tx", "cc", "multicastId")
        val debugConfig = DebugConfig()
        debugConfig.sendSLogo = 0.0
        debugConfig.sendLogo = 0.0
        debugConfig.sendWebStreams = 0.0
        debugConfig.sendBearerList = 0.0
        debugConfig.minDelay = 20
        debugConfig.maxDelay = 21
        debugController.setDebugConfig(debugConfig)
        val callback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)
        val timeBefore = timeProvider.getCurrentTimeMillis()
        debugController.getRadioStationData(mediaRequestObject)

        val metadata = callback.metadata
        val timeAfter = timeProvider.getCurrentTimeMillis()
        assertNotNull(metadata)
        val timeDif = timeAfter - timeBefore
        assertTrue(timeDif >= 20000)
    }

    @Test
    fun callculate_the_correct_expireDate() {
        val bearer = HdBearer("tx", "cc", "multicastId")
        val debugConfig = DebugConfig()
        debugConfig.sendSLogo = 0.0
        debugConfig.sendLogo = 0.0
        debugConfig.sendMetadata = 0.0
        debugConfig.sendWebStreams = 0.0
        debugConfig.sendBearerList = 0.0
        debugConfig.expireInSeconds = 123456789L
        debugController.setDebugConfig(debugConfig)
        val callback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)
        debugController.getRadioStationData(mediaRequestObject)

        assertNotNull(callback.completeStatus)
        assertNotNull(callback.expirationDate)
        assertEquals(
            timeProvider.getCurrentTimeSeconds() + 123456789L,
            callback.expirationDate
        )
    }

    @Test
    fun send_delete_for_all_data() {
        val bearer = HdBearer("tx", "cc", "multicastId")
        val debugConfig = DebugConfig()
        debugConfig.deleteSLogo = true
        debugConfig.deleteLogo = true
        debugConfig.deleteMetadata = true
        debugConfig.deleteWebStreams = true
        debugConfig.deleteBearerList = true
        debugController.setDebugConfig(debugConfig)
        val callback = TestRadioStationDataCallback()
        val eTags = ETags(null, null, null, null, null)
        val mediaRequestObject = MediaRequestObject(bearer, eTags, callback)
        debugController.getRadioStationData(mediaRequestObject)

        assertNull(callback.metadata)
        assertNull(callback.logo)
        assertNull(callback.sLogo)
        assertNull(callback.bearerList)
        assertNull(callback.streamList)
        assertNotNull(callback.completeStatus)
        assertEquals(RetryBehavior.NONE, callback.completeStatus!!.retry)
        assertNotNull(callback.expirationDate)
        val expectedCallFunctions =
            TestRadioStationDataCallback.METADATA or
                    TestRadioStationDataCallback.BEARERS or
                    TestRadioStationDataCallback.STREAMS or
                    TestRadioStationDataCallback.LOGO or
                    TestRadioStationDataCallback.SLOGO or
                    TestRadioStationDataCallback.COMPLETE
        assertEquals(expectedCallFunctions, callback.calledFunctions)
    }
}