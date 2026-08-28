package de.esolutions.connectedradioclient.handlers.backend.middleware

import androidx.annotation.VisibleForTesting
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.util.Calendar

object ServerDowntimeHandler {

    @VisibleForTesting
    var serverDowntime: Calendar? = null

    /**
     * Handles the response from the server and sets the server downtime if a 'Retry-After' header is present.
     * This function is intended to be used for status codes 429 (Too Many Requests) and 503 (Service Unavailable).
     *
     * @param retryAfter The value of the 'Retry-After' header in seconds. If null, no action is taken.
     *                   The 'Retry-After' header indicates how long the client should wait before making a new request.
     */
    fun handleResponse(retryAfter: Long?) {
        if (retryAfter != null) {
            setServerDowntime(retryAfter)
        }
    }

    /**
     * Sets how long the server is not available.
     */
    fun setServerDowntime(seconds: Long) {
        val logMessage = "Assume server downtime for $seconds seconds"
        Logger.i(PrivacyFlags.SAFE, null, logMessage, LoggerTags.RESPONSE)

        // Use setTimeInMillis() instead of add(), because add() only accepts Integer and not Long.
        val timestamp = Calendar.getInstance()
        timestamp.timeInMillis += seconds * 1000

        serverDowntime = timestamp
    }

    /**
     * Returns in how many seconds the server is available again or returns null if the request can start now.
     */
    fun getRemainingDowntime(): Long? {
        serverDowntime?.let {
            val remainingDowntime = (it.timeInMillis - Calendar.getInstance().timeInMillis) / 1000

            if (remainingDowntime > 0) {
                val logMessage = "Remaining server downtime of $remainingDowntime seconds"
                Logger.i(PrivacyFlags.SAFE, null, logMessage, LoggerTags.REQUEST)

                return remainingDowntime
            }
        }
        return null
    }
}