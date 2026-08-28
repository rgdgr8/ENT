package technology.cariad.entertainment.metadataservice.domain.entity

import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy

/**
 * Entity class for a radio station or cover art request containing all known information about
 * object life cycle.
 *
 * @param lastRequested the time at which the station was last requested.
 * @param expirationDate the date at which the station entity becomes outdated and should be
 *                       checked for updates. Seconds since epoch (UTC Unix time)
 * @param retryStrategy strategy for retries
 * @param retryDelay delay in seconds depending on [retryStrategy]
 * @param retryCount number of consecutive unsuccessful retries.
 */

data class LifeCycleDataEntity (
    var lastRequested: Long = 0,
    var expirationDate: Long? = null,
    var retryStrategy: RetryStrategy? = null,
    var retryDelay: Long? = null,
    var retryCount: Int? = null,
    var retryAfter: Long? = null
)
