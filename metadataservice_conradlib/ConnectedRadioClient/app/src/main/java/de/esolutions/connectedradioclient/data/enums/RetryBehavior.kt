package de.esolutions.connectedradioclient.data.enums

enum class RetryBehavior {
    /**
     * The bearer is only requested again after:
     * - When it has flown out of the cache (because of the maximum length)
     * - The client requests again and the expiration date is expired or null.
     */
    NONE,

    /**
     * The bearer will be requested once again.
     * This works by an internal retry counter in the MDS.
     */
    ONCE,

    /**
     * The bearer will be requested in an endless loop.
     * The retry delay is defined by the ConRadClient.
     */
    DELAYED,

    /**
     * The bearer will be requested in an endless loop.
     * The request delay increases with each request.
     */
    EXPONENTIAL
}