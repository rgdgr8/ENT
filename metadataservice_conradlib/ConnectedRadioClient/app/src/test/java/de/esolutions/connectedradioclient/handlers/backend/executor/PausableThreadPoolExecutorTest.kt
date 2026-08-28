package de.esolutions.connectedradioclient.handlers.backend.executor

import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.helper.TestBlockingJob
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class PausableThreadPoolExecutorTest {

    private val testQueue = LinkedBlockingQueue<Runnable>()
    private val pausableThreadPoolExecutor = PausableThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.SECONDS,
        testQueue
    )
    private val timeProvider: TimeProvider = DefaultTimeProvider()
    private var runJob = false
    private var runOrder = mutableListOf<Int>()

    @BeforeEach
    fun before() {
        runJob = false
        runOrder = mutableListOf()
        testQueue.clear()
    }

    @Test
    fun dont_execute_job_on_pause() {
        pausableThreadPoolExecutor.pause()
        assertTrue(pausableThreadPoolExecutor.isPaused())
        assertEquals(0, testQueue.size)

        pausableThreadPoolExecutor.submit(TestBlockingJob { runJob = true })
        pausableThreadPoolExecutor.submit(TestBlockingJob { runJob = true })
        pausableThreadPoolExecutor.submit(TestBlockingJob { runJob = true })
        pausableThreadPoolExecutor.submit(TestBlockingJob { runJob = true })

        //the first job is removed from the queue but not executed
        assertEquals(3, testQueue.size)
        assertFalse(runJob)
    }

    @Test
    fun run_jobs_in_queue_in_correct_order() {
        pausableThreadPoolExecutor.resume()
        assertFalse(pausableThreadPoolExecutor.isPaused())
        assertEquals(0, testQueue.size)

        var lastJobFinished = false
        val latch = CountDownLatch(1)
        pausableThreadPoolExecutor.submit(TestBlockingJob(latch) {
            runJob = true
            runOrder.add(1)
        })
        for (i in 2..9) {
            pausableThreadPoolExecutor.submit(TestBlockingJob {
                runJob = true
                runOrder.add(i)
            })
        }
        pausableThreadPoolExecutor.submit(TestBlockingJob {
            runJob = true
            runOrder.add(10)
            lastJobFinished = true
        })
        latch.countDown()

        val currentTime = timeProvider.getCurrentTimeSeconds()
        while (!lastJobFinished) {
            if (timeProvider.getCurrentTimeSeconds() - currentTime > 2) {
                fail("Queue took too much time")
            }
        }

        assertTrue(runJob)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), runOrder)
    }
}