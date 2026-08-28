package de.esolutions.connectedradioclient.helper

import de.esolutions.connectedradioclient.handlers.backend.jobs.IJob
import java.util.concurrent.CountDownLatch

class TestBlockingJob(
    private val latch: CountDownLatch? = null,
    private val callback: () -> Unit = { }
) : IJob {

    override fun run() {
        latch?.await()
        callback()
    }
}