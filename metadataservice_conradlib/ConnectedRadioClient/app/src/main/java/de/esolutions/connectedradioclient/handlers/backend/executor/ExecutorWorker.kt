package de.esolutions.connectedradioclient.handlers.backend.executor

import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

object ExecutorWorker {

    private val pausableThreadPoolExecutor: PausableThreadPoolExecutor =
        PausableThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue())

    /**
     * resume the thread pool executor.
     */
    fun start() {
        Logger.i(PrivacyFlags.SAFE, null, "Start ThreadPoolExecutor", LoggerTags.EXECUTOR)
        pausableThreadPoolExecutor.resume()
    }

    /**
     * set the thread pool executor to pause.
     */
    fun stop() {
        Logger.i(PrivacyFlags.SAFE, null, "Stop ThreadPoolExecutor", LoggerTags.EXECUTOR)
        pausableThreadPoolExecutor.pause()
    }

    /**
     * enqueue the given [job] to the thread pool queue
     */
    fun enqueue(job: Runnable) {
        Logger.i(
            PrivacyFlags.SAFE,
            null,
            "Add new ${job::class.java.simpleName} to queue",
            LoggerTags.EXECUTOR
        )
        pausableThreadPoolExecutor.submit(job)
    }

    /**
     * return the status of the worker.
     * true = worker is paused
     * false = worker is running
     */
    fun isPaused(): Boolean {
        return pausableThreadPoolExecutor.isPaused()
    }
}