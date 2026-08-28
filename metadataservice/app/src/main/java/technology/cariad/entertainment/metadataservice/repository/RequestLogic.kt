package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.domain.value.RetryStrategy
import kotlin.math.min
import kotlin.math.pow

object RequestLogic {

    private val log by logger()

    private const val SECONDS_PER_DAY = 24 * 60 * 60
    private const val MAX_DELAYED_RETRIES = 3

    /**
     * Evaluates the cached station taking the current time into account and checks if the station
     * data is still up-to-date. A station needs to be updated either if it is missing entirely,
     * if no past request had been successfully completed or if its expiration date has passed.
     */
    fun updateRequired(cached: LifeCycleDataEntity, currentTime: Long): Boolean {

        // If a retry is scheduled and hasn't expired yet, no update needed
        val retryPending = cached.retryAfter != null && cached.retryAfter!! > currentTime
        if (retryPending) {
            log.info(this::updateRequired.name) {
                "update=false (retry pending, retryAfter=${cached.retryAfter}, now=$currentTime)"
            }
            return false
        }

        val isPresent = cached.expirationDate != null
        val isExpired = (cached.expirationDate ?: 0) < currentTime

        val isOutdated = (!isPresent || isExpired)

        log.info(this::updateRequired.name) {
            "update=$isOutdated (present=$isPresent, expired=$isExpired)"
        }

        log.debug(this::updateRequired.name) {
            "current time=$currentTime, expires=${cached.expirationDate}"
        }

        return isOutdated
    }

    /**
     * Evaluates the cached station taking the current time into account and checks if new station
     * data is allowed to be requested. To reduce both network and backend load, station data
     * updates must be limited as much as possible. Updates are only allowed after any previously
     * propagated retry delays have been exceeded or if no retries are active.
     */
    fun updatedAllowed(cached: LifeCycleDataEntity, currentTime: Long): Boolean {

        //Ensure the next retry is not more than 48hours in the future
        val forceUpdate = cached.retryAfter?.let { retryAfter ->

            if (retryAfter - currentTime > 2 * SECONDS_PER_DAY) {
                cached.retryAfter = currentTime

                log.warn(this::updatedAllowed.name) {
                    "retry after exceeds limit of 48 hours, force update:" +
                            " retryAfter=$retryAfter currentTime=$currentTime"
                }

                true
            } else {
                false
            }
        } ?: false

        val isAllowed = ((cached.retryAfter ?: 0) <= currentTime) || forceUpdate

        log.info(this::updatedAllowed.name) { "update allowed=$isAllowed" }

        log.debug(this::updatedAllowed.name) {
            "current time=$currentTime, last modified=${cached.retryAfter}"
        }

        return isAllowed
    }

    internal fun retryDelay(
        cached: LifeCycleDataEntity,
        status: LifeCycleDataEntity,
        currentTime: Long
    ): LifeCycleDataEntity {

        val strategyChanged = cached.retryStrategy != status.retryStrategy

        if (strategyChanged) {
            status.retryCount = 0
        }

        var retryDelaySeconds: Long? = null

        when (status.retryStrategy) {
            //success
            RetryStrategy.NONE -> {
                status.retryCount = 0
                retryDelaySeconds = null
            }

            RetryStrategy.ONCE -> {
                if ((cached.retryCount ?: 0) == 0) {
                    status.retryCount = (cached.retryCount?.inc() ?: 1)
                    retryDelaySeconds = status.retryDelay!!
                } else {
                    status.retryCount = 1
                    retryDelaySeconds = null
                }
            }

            RetryStrategy.DELAYED -> {
                status.retryCount = (cached.retryCount?.inc() ?: 1)
                if (status.retryCount!! > MAX_DELAYED_RETRIES) {
                    log.info(this::retryDelay.name) {
                        "max delayed retries ($MAX_DELAYED_RETRIES) reached, " +
                                "switching to daily retry"
                    }
                    retryDelaySeconds = SECONDS_PER_DAY.toLong()
                } else {
                    retryDelaySeconds = status.retryDelay!!
                }
            }

            RetryStrategy.EXPONENTIAL -> {
                status.retryCount = (cached.retryCount?.inc() ?: 1)
                retryDelaySeconds =
                    (status.retryDelay!! * 2.0.pow(status.retryCount!! - 1)).toLong()
            }

            null -> Unit
        }

        //Ensure the next retry is not more than 48hours in the future;
        status.retryAfter = if (retryDelaySeconds != null) {
            val adjustedRetryDelaySeconds = min(retryDelaySeconds, 2L * SECONDS_PER_DAY)

            if (adjustedRetryDelaySeconds != retryDelaySeconds) {
                log.warn(this::retryDelay.name) {
                    "new retry delay exceeds limit of 48 hours: " +
                            "delay=$retryDelaySeconds adjusted=$adjustedRetryDelaySeconds"
                }
            }
            currentTime + adjustedRetryDelaySeconds
        } else null

        log.info(this::retryDelay.name) {
            "updated retry delay: " +
                    "retryAfter=${status.retryAfter} " +
                    "strategy=${status.retryStrategy} " +
                    "retryCount=${status.retryCount} " +
                    "retryDelay=${status.retryDelay} "
        }

        return status
    }
}

