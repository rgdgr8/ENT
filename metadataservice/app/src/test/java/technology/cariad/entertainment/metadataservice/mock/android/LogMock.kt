package technology.cariad.entertainment.metadataservice.mock.android

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.slot

/**
 * Mocks Android logging facility for plain unit tests.
 * Instantiate either during setup or inside of test case.
 * It is not necessary to keep a reference to the object.
 */
@Deprecated(
    "Outdated with switch to framework logging",
    ReplaceWith(
        "LoggerMock",
        "technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock"
    )
)
class LogMock {
    init {
        mockkStatic(Log::class)
        val slotTag = slot<String>()
        val slotMsg = slot<String>()

        every {
            Log.v(capture(slotTag), capture(slotMsg))
        } answers { println("v: ${slotTag.captured} : ${slotMsg.captured}"); 0 }

        every {
            Log.d(capture(slotTag), capture(slotMsg))
        } answers { println("d: ${slotTag.captured} : ${slotMsg.captured}"); 0 }

        every {
            Log.i(capture(slotTag), capture(slotMsg))
        } answers { println("i: ${slotTag.captured} : ${slotMsg.captured}"); 0 }

        every {
            Log.w(capture(slotTag), capture(slotMsg))
        } answers { println("w: ${slotTag.captured} : ${slotMsg.captured}"); 0 }

        every {
            Log.e(capture(slotTag), capture(slotMsg))
        } answers { println("e: ${slotTag.captured} : ${slotMsg.captured}"); 0 }

        every {
            Log.wtf(capture(slotTag), capture(slotMsg))
        } answers { println("wtf: ${slotTag.captured} : ${slotMsg.captured}"); 0 }

        every { Log.w(any(), any<Throwable>()) } returns 0
        every { Log.wtf(any(), any<Throwable>()) } returns 0
    }
}

