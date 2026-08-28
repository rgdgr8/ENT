package technology.cariad.entertainment.metadataservice.mock.common.log

import android.util.Log
import technology.cariad.entertainment.metadataservice.common.log.Logger
import io.mockk.every
import io.mockk.invoke
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import kotlin.reflect.KClass

/**
 * LOCAL BUILD: Updated LoggerMock for Android Log based implementation.
 * 
 * Mocks framework logging facility for plain unit tests.
 * Instantiate either during setup or inside of test case.
 * It is not necessary to keep a reference to the object.
 */
class LoggerMock {

    init {
        // Mock Android Log static methods
        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
        every { Log.e(any(), any()) } returns 0
        every { Log.wtf(any(), any<String>()) } returns 0
        every { Log.isLoggable(any(), any()) } returns true

        mockkObject(Logger)

        val classSlot = slot<KClass<*>>()

        every { Logger.get(capture(classSlot), any()) } answers {
            val logger = callOriginal()
            val loggerSpy = spyk(logger)

            val levelSlot = slot<Int>()
            val funcSlot = slot<String>()

            every {
                loggerSpy.log(
                    capture(levelSlot),
                    capture(funcSlot),
                    any(),
                    captureLambda()
                )
            } answers {
                val levelName = when(levelSlot.captured) {
                    Log.VERBOSE -> "TRACE"
                    Log.DEBUG -> "DEBUG"
                    Log.INFO -> "INFO"
                    Log.WARN -> "WARN"
                    Log.ERROR -> "ERROR"
                    Log.ASSERT -> "FATAL"
                    else -> "UNKNOWN"
                }
                println(
                    "$levelName: " +
                    "[${classSlot.captured.simpleName}] " +
                    "${funcSlot.captured}: ${lambda<() -> String>().invoke()}"
                )
            }
            loggerSpy
        }
    }
}

