package de.esolutions.connectedradioclient.debug.data

import de.esolutions.connectedradioclient.data.CompleteStatus
import de.esolutions.connectedradioclient.data.enums.RetryBehavior

class DemoCompleteStatus {

    /**
     * Returns demo success status.
     */
    fun getSuccessStatus(): CompleteStatus {
        return CompleteStatus(RetryBehavior.NONE, null)
    }

    /**
     * Returns demo error status.
     */
    fun getErrorStatus(): CompleteStatus {
        return CompleteStatus(RetryBehavior.ONCE, 20L)
    }
}