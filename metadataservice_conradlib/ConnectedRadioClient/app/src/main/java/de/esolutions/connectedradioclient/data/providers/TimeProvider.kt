package de.esolutions.connectedradioclient.data.providers

/**
 * Provides the current time in milliseconds and seconds which is useful for testing.
 */
interface TimeProvider {
    /**
     * @return The current time in milliseconds since the epoch (UTC).
     */
    fun getCurrentTimeMillis(): Long

    /**
     * @return The current time in seconds since the epoch (UTC).
     */
    fun getCurrentTimeSeconds(): Long
}

/**
 * Default implementation of the [TimeProvider] interface using [System.currentTimeMillis].
 */
class DefaultTimeProvider : TimeProvider {
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()

    override fun getCurrentTimeSeconds(): Long = System.currentTimeMillis() / 1000L
}