package de.esolutions.connectedradioclient.handlers.backend

import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.MediaRequestObject
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.handlers.backend.executor.ExecutorWorker
import de.esolutions.connectedradioclient.handlers.backend.jobs.LookupRequestJob
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import de.esolutions.connectedradioclient.handlers.coreservices.TokenHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LookupRequestContainer(
    private val registrationHandler: HandleRegistration,
    private val tokenHandler: TokenHandler,
    private val mcc: String?
) {

    private val timeProvider: TimeProvider = DefaultTimeProvider()
    private var firstRequestTimestamp: Long = timeProvider.getCurrentTimeSeconds()
    private var requestContainer: MutableMap<String, MediaRequestObject> = HashMap()
    private var debounceJob: Job? = null
    private var isLocked: Boolean = false

    /**
     * Add the given [mediaRequestObject] to the container and start the request, if any limit is hit
     * or start the debounce-job to start the request after 500 milliseconds.
     */
    @Synchronized
    fun addRequest(mediaRequestObject: MediaRequestObject) {
        requestContainer[mediaRequestObject.bearer.getBearerString()] = mediaRequestObject
        if (hitAnyLimit()) {
            lockAndStartRequest()
        } else {
            debounceJob = CoroutineScope(Dispatchers.Default).launch {
                delay((Config.requestConfig.batchDebounce * 1000).toLong())
                lockAndStartRequest()
            }
        }
    }

    /**
     * Cancel the current debounceJob and return true if not locked.
     * Return false if the container is already locked.
     */
    @Synchronized
    fun cancelDebounceJob(): Boolean {
        return if (!isLocked) {
            debounceJob?.cancel()
            true
        } else {
            false
        }
    }

    /**
     * Return true if any limit is hit. Otherwise, return false.
     */
    private fun hitAnyLimit(): Boolean {
        return hitSizeLimit() || hitTotalTimeLimit()
    }

    /**
     * Lock the container and start the request.
     * Ignore the call if the container is already locked.
     */
    @Synchronized
    private fun lockAndStartRequest() {
        if (!isLocked) {
            isLocked = true
            val job = LookupRequestJob(requestContainer, mcc, registrationHandler, tokenHandler)
            ExecutorWorker.enqueue(job)
        }
    }

    /**
     * Check if the max time limit is reached.
     */
    private fun hitTotalTimeLimit(): Boolean {
        return (timeProvider.getCurrentTimeSeconds() - firstRequestTimestamp) >= Config.requestConfig.batchMaxWait
    }

    /**
     * Check if the max size limit is reached.
     */
    private fun hitSizeLimit(): Boolean {
        return requestContainer.size >= Config.requestConfig.batchSize
    }
}