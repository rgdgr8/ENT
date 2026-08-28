package technology.cariad.entertainment.metadataservice.common.log

import android.content.Context
import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * LOCAL BUILD: Updated LoggerTest for Android Log based implementation
 */
class LoggerTest {

    class TestClassOne
    class TestClassTwo
    class ClassWithCompanion {
        companion object
    }

    private val contextMock = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.wtf(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns true
    }

    @Test
    fun init_shouldLogInitialization() {
        Logger.init(contextMock)

        verify {
            Log.i(any(), match { it.contains("Logger initialized") })
        }
    }

    @Test
    fun deinit_shouldLogDeinitialization() {
        Logger.deinit()

        verify {
            Log.i(any(), match { it.contains("Logger deinitialized") })
        }
    }

    @Test
    fun anyLogger_shouldReturnUniqueLoggerForSameClass() {
        val loggerOne = TestClassOne().logger(null).value
        val loggerTwo = TestClassOne().logger(null).value

        Assert.assertSame(loggerOne, loggerTwo)
    }

    @Test
    fun anyLogger_shouldReturnDifferentLoggersForDifferentClasses() {
        val loggerOne = TestClassOne().logger(null).value
        val loggerTwo = TestClassTwo().logger(null).value

        Assert.assertNotSame(loggerOne, loggerTwo)
    }

    @Test
    fun logger_shouldReturnUniqueLoggerForSameClass() {
        val loggerOne = logger(TestClassOne::class, null).value
        val loggerTwo = logger(TestClassOne::class, null).value

        Assert.assertSame(loggerOne, loggerTwo)
    }

    @Test
    fun logger_shouldReturnDifferentLoggersForDifferentClasses() {
        val loggerOne = logger(TestClassOne::class, null).value
        val loggerTwo = logger(TestClassTwo::class, null).value

        Assert.assertNotSame(loggerOne, loggerTwo)
    }

    @Test
    fun logger_anyLogger_shouldReturnSameLoggerForSameClass() {
        val test = TestClassOne()

        val loggerOne = test.logger(null).value
        val loggerTwo = logger(TestClassOne::class, null).value

        Assert.assertSame(loggerOne, loggerTwo)
    }

    @Test
    fun logShortHands_shouldUseCorrectLogLevel() {
        val logger = Logger.get(TestClassOne::class, null)

        // With Android Log, just verify no exceptions are thrown
        logger.trace("func", null) { "message" }
        logger.debug("func", null) { "message" }
        logger.info("func", null) { "message" }
        logger.warn("func", null) { "message" }
        logger.error("func", null) { "message" }
        logger.fatal("func", null) { "message" }

        // Verify Android Log methods were called
        verify { Log.v(any(), any()) }
        verify { Log.d(any(), any()) }
        verify { Log.i(any(), any()) }
        verify { Log.w(any(), any<String>()) }
        verify { Log.e(any(), any()) }
        verify { Log.wtf(any(), any<String>()) }
    }

    @Test
    fun get_shouldNotFailForAnonymousClass() {
        Logger.get((object {})::class)
    }

    @Test
    fun get_shouldNotFailForCompanionClass() {
        Logger.get(ClassWithCompanion.Companion::class)
    }
}

