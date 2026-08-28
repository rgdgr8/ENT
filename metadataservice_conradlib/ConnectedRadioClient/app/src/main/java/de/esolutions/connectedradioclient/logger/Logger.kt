package de.esolutions.connectedradioclient.logger

import android.util.Log
import androidx.annotation.VisibleForTesting
import de.esolutions.connectedradioclient.data.extensions.toSimpleString
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Local replacement for de.esolutions.fw.util.commons.tracing.stapi.TraceLevels
 */
enum class TraceLevels {
    TRACE, DEBUG, INFO, WARN, ERROR
}

object Logger {

    private const val CHANNEL_CONRAD_CLIENT = "connectedradioclient"
    private var logTag: String = CHANNEL_CONRAD_CLIENT

    /**
     * init tracer and set logChannelName
     */
    fun init(loggerChannelRoot: String) {
        logTag = "$loggerChannelRoot.$CHANNEL_CONRAD_CLIENT"
    }

    /**
     * log trace
     */
    fun t(pf: PrivacyFlags, logInfo: LogInfo?, message: String, vararg tags: LoggerTags) {
        log(TraceLevels.TRACE, pf, logInfo, message, *tags)
    }

    /**
     * log debug
     */
    fun d(pf: PrivacyFlags, logInfo: LogInfo?, message: String, vararg tags: LoggerTags) {
        log(TraceLevels.DEBUG, pf, logInfo, message, *tags)
    }

    /**
     * log info
     */
    fun i(pf: PrivacyFlags, logInfo: LogInfo?, message: String, vararg tags: LoggerTags) {
        log(TraceLevels.INFO, pf, logInfo, message, *tags)
    }

    /**
     * log warning
     */
    fun w(pf: PrivacyFlags, logInfo: LogInfo?, message: String, vararg tags: LoggerTags) {
        log(TraceLevels.WARN, pf, logInfo, message, *tags)
    }

    /**
     * log error without exceptions
     */
    fun e(pf: PrivacyFlags, logInfo: LogInfo?, message: String, vararg tags: LoggerTags) {
        log(TraceLevels.ERROR, pf, logInfo, message, *tags)
    }

    /**
     * log error with exceptions including class, message and caused exception
     */
    fun ex(
        pf: PrivacyFlags,
        logInfo: LogInfo?,
        msg: String?,
        tr: Throwable,
        vararg tags: LoggerTags
    ) {
        val exceptionMessage = tr.toSimpleString()
        val logText = if (msg.isNullOrEmpty()) exceptionMessage else "$msg $exceptionMessage"
        log(TraceLevels.ERROR, pf, logInfo, logText, *tags)
    }

    /**
     * create the log message and send them to android.util.Log
     */
    private fun log(
        level: TraceLevels,
        pf: PrivacyFlags,
        logInfo: LogInfo?,
        message: String,
        vararg tags: LoggerTags
    ) {
        val logMessage = buildLogMessage(logInfo, message, tags = tags)
        val taggedMessage = "[${pf.name}] $logMessage"

        when (level) {
            TraceLevels.TRACE -> Log.v(logTag, taggedMessage)
            TraceLevels.DEBUG -> Log.d(logTag, taggedMessage)
            TraceLevels.INFO -> Log.i(logTag, taggedMessage)
            TraceLevels.WARN -> Log.w(logTag, taggedMessage)
            TraceLevels.ERROR -> Log.e(logTag, taggedMessage)
        }
    }

    /**
     * Trim the given String [token] and return the first and second part joined with ".".
     * It returns an empty String if the [token] is null, empty or to short.
     */
    fun trimToken(token: String?): String {
        val trimmedToken = token?.let {
            val tokenParts = token.split(".")
            if (tokenParts.size >= 2) tokenParts.subList(0, 2).joinToString(".") else ""
        } ?: ""
        return trimmedToken
    }

    /**
     * Builds a log message by the given attributes.
     */
    @VisibleForTesting
    fun buildLogMessage(logInfo: LogInfo?, message: String, vararg tags: LoggerTags): String {
        val bearerTag = logInfo?.bearer?.let { "[$it] " } ?: ""
        val hashTagsString = if (tags.isEmpty()) "" else " " + tags.joinToString(" ")
        val requestIdTag = logInfo?.requestId?.let { " #request:$it" } ?: ""

        val buildMessage = bearerTag + message + hashTagsString + requestIdTag
        return "$buildMessage #crc"
    }

    private fun getTimestampString(): String {
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd_HH:mm:ss")
            .withZone(ZoneOffset.systemDefault())
            .format(Instant.now())
    }
}