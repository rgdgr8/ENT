package de.esolutions.connectedradioclient.handlers.backend.executor

import de.esolutions.connectedradioclient.helper.ModifierAccessHacker
import de.esolutions.connectedradioclient.helper.TestBlockingJob
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class ExecutorWorkerTest {

    private val mockPausableThreadPoolExecutor = mock(PausableThreadPoolExecutor::class.java)

    @BeforeEach
    fun before() {
        val privatePausableThreadPoolExecutor =
            ExecutorWorker::class.java.getDeclaredField("pausableThreadPoolExecutor")
        privatePausableThreadPoolExecutor.isAccessible = true

        val modifier = ModifierAccessHacker.modifier
        modifier.isAccessible = true
        modifier.setInt(
            privatePausableThreadPoolExecutor,
            privatePausableThreadPoolExecutor.modifiers and Modifier.FINAL.inv()
        )

        privatePausableThreadPoolExecutor.set(ExecutorWorker, mockPausableThreadPoolExecutor)
    }

    @Test
    fun call_resume() {
        ExecutorWorker.start()
        verify(mockPausableThreadPoolExecutor).resume()
    }

    @Test
    fun call_pause() {
        ExecutorWorker.stop()
        verify(mockPausableThreadPoolExecutor).pause()
    }

    @Test
    fun call_execute() {
        val testJob = TestBlockingJob()
        ExecutorWorker.enqueue(testJob)
        verify(mockPausableThreadPoolExecutor).submit(testJob)
    }

    @Test
    fun call_isPaused() {
        ExecutorWorker.isPaused()
        verify(mockPausableThreadPoolExecutor).isPaused()
    }

}