package de.esolutions.connectedradioclient.data

import de.esolutions.connectedradioclient.data.enums.RetryBehavior

data class CompleteStatus(
    val retry: RetryBehavior,
    val retryDelaySeconds: Long?
)