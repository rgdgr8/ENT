package de.esolutions.connectedradioclient.handlers.backend.executor

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

/**
 * Works like the ThreadPoolExecutor but have the ability to pause before executing.
 * Code adaption from the documentation (03.05.2021):
 * https://developer.android.com/reference/java/util/concurrent/ThreadPoolExecutor
 */
class PausableThreadPoolExecutor(
    corePoolSize: Int,
    maximumPoolSize: Int,
    keepAliveTime: Long,
    unit: TimeUnit,
    blockingQueue: BlockingQueue<Runnable>
) : ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, blockingQueue) {

    private var isPaused: Boolean = true
    private val pauseLock: ReentrantLock = ReentrantLock()
    private val unpause: Condition = pauseLock.newCondition()

    override fun beforeExecute(t: Thread?, r: Runnable?) {
        super.beforeExecute(t, r)
        pauseLock.lock()
        try {
            while (isPaused) unpause.await()
        } catch (ie: InterruptedException) {
            t?.interrupt()
        } finally {
            pauseLock.unlock()
        }
    }

    /**
     * Pause the executor.
     */
    fun pause() {
        pauseLock.lock()
        try {
            this.isPaused = true
        } finally {
            pauseLock.unlock()
        }
    }

    /**
     * Resume the executor.
     */
    fun resume() {
        pauseLock.lock()
        try {
            this.isPaused = false
            unpause.signalAll()
        } finally {
            pauseLock.unlock()
        }
    }

    /**
     * Returns true if the executor is paused.
     */
    fun isPaused(): Boolean {
        return this.isPaused
    }
}