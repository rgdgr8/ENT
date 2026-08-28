/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 *
 * Modifications:
 * - 2026-02-06: Replaced eso TraceClient with Android native logging (android.util.Log)
 */
package technology.cariad.entertainment.metadataservice.common.log

import android.content.Context
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

/**
 * LOCAL BUILD VERSION: Android-native logging implementation.
 * 
 * This class replaces the eso framework TraceClient with standard Android Log.
 * 
 * Instances of this class can be obtained through the [Any.logger] extension method or the free
 * function [logger].
 *
 * Creating a [Logger] using [Any.logger] derives the channel name and the class name from the fully
 * qualified name of the concrete class type, the function is called on.
 *
 * This mechanism doesn't work in all cases, it fails e.g. when [Any] is an anonymous classes
 * or object expressions. In this case either the [Logger] from the outer class can be used or a
 * new [Logger] instance based on any known class can be constructed using the free function
 * [logger]. Channel and class name are then derived from this passed class.
 *
 * It is possible to define a default [PrivacyFlag], when obtaining the logger. If no flag
 * is passed, the logger uses the internally defined default flag
 * ([PrivacyFlag.CATEGORY_UNDECIDED] or [PrivacyFlag.OWNER_UNDECIDED]). In addition it is
 * possible to override the flag for each log message.
 *
 * A single [Logger] instance is cached per class. If an instance for a class has already been
 * created and is requested with a different [PrivacyFlag], this flag is discarded.
 * If you find a good reason to attach a different [PrivacyFlag] to logs from objects of the same
 * class feel free to rework this behavior.
 *
 * All log methods take the log message as lambda function. This allows for the evaluation of string
 * templates to be done lazily by the underlying logging. If depending on the configured
 * log level, a log message is not written out, the string template is never evaluated.
 */
class Logger private constructor(
    channelName: String,
    private val className: String,
    privacyFlag: PrivacyFlag? = null
) {
    companion object {
        const val PROCESS_NAME = "metadataservice"
        private const val PACKAGE_BASE = "technology.cariad.entertainment."
        private const val MAX_TAG_LENGTH = 23 // Android Log tag limit

        private val loggers = ConcurrentHashMap<KClass<out Any>, Logger>()

        /**
         * Initializes the [Logger] using the given [Context].
         * LOCAL BUILD: This is a no-op for Android Log.
         * @param context
         */
        fun init(context: Context) {
            // No-op for Android native logging
            Log.i(PROCESS_NAME, "Logger initialized (LOCAL BUILD - Android Log)")
        }

        /**
         * De-initializes the [Logger].
         * LOCAL BUILD: This is a no-op for Android Log.
         */
        fun deinit() {
            // No-op for Android native logging
            Log.i(PROCESS_NAME, "Logger deinitialized (LOCAL BUILD - Android Log)")
        }

        internal fun <T : Any> get(klazz: KClass<T>, privacyFlag: PrivacyFlag? = null): Logger {
            return loggers.computeIfAbsent(klazz) {
                val outer = stripCompanionClass(klazz)
                val className = outer.simpleName ?: "UNKNOWN_CLASS"
                val channelName = channelNameForClass(outer) ?: "UNKNOWN_PACKAGE"
                Logger(channelName, className, privacyFlag)
            }
        }

        private fun <T : Any> stripCompanionClass(klazz: KClass<T>): KClass<*> {
            return stripCompanionClass(klazz.java).kotlin
        }

        private fun <T : Any> stripCompanionClass(clazz: Class<T>): Class<*> {
            return clazz.enclosingClass?.takeIf {
                it.kotlin.companionObject?.java == clazz
            } ?: clazz
        }

        private fun <T : Any> channelNameForClass(klazz: KClass<T>): String? {
            return klazz.java.`package`?.name?.removePrefix(PACKAGE_BASE)
        }
    }

    // Create Android Log tag: "mds/channel" truncated to 23 chars
    private val tag: String = run {
        val fullTag = "mds/$channelName"
        if (fullTag.length > MAX_TAG_LENGTH) {
            fullTag.substring(0, MAX_TAG_LENGTH)
        } else {
            fullTag
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private val defaultPrivacyFlag = privacyFlag ?: PrivacyFlag.NOT_CATEGORIZED

    /**
     * Logs trace level messages (VERBOSE in Android).
     * @see [log]
     */
    fun trace(function: String, privacyFlag: PrivacyFlag? = null, message: () -> String) {
        if (Log.isLoggable(tag, Log.VERBOSE)) {
            Log.v(tag, formatMessage(function, message()))
        }
    }

    /**
     * Logs debug level messages.
     * @see [log]
     */
    fun debug(function: String, privacyFlag: PrivacyFlag? = null, message: () -> String) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, formatMessage(function, message()))
        }
    }

    /**
     * Logs information level messages.
     * @see [log]
     */
    fun info(function: String, privacyFlag: PrivacyFlag? = null, message: () -> String) {
        Log.i(tag, formatMessage(function, message()))
    }

    /**
     * Logs warning level messages.
     * @see [log]
     */
    fun warn(function: String, privacyFlag: PrivacyFlag? = null, message: () -> String) {
        Log.w(tag, formatMessage(function, message()))
    }

    /**
     * Logs error level messages.
     * @see [log]
     */
    fun error(function: String, privacyFlag: PrivacyFlag? = null, message: () -> String) {
        Log.e(tag, formatMessage(function, message()))
    }

    /**
     * Logs fatal level messages (WTF in Android - What a Terrible Failure).
     * @see [log]
     */
    fun fatal(function: String, privacyFlag: PrivacyFlag? = null, message: () -> String) {
        Log.wtf(tag, formatMessage(function, message()))
    }

    /**
     * Logs messages at the specified level.
     * LOCAL BUILD: Maps TraceLevels to Android Log levels.
     * @param level Importance level of the log (as Int: VERBOSE=2, DEBUG=3, INFO=4, WARN=5, ERROR=6, ASSERT=7)
     * @param function Name of the function
     * @param privacyFlag Flag for setting the privacy category (ignored in local build)
     * @param message Log message
     */
    fun log(
        level: Int,
        function: String,
        privacyFlag: PrivacyFlag? = null,
        message: () -> String
    ) {
        val msg = formatMessage(function, message())
        when (level) {
            Log.VERBOSE -> Log.v(tag, msg)
            Log.DEBUG -> Log.d(tag, msg)
            Log.INFO -> Log.i(tag, msg)
            Log.WARN -> Log.w(tag, msg)
            Log.ERROR -> Log.e(tag, msg)
            Log.ASSERT -> Log.wtf(tag, msg)
            else -> Log.d(tag, msg)
        }
    }

    private fun formatMessage(function: String, message: String): String {
        return "[$className] $function: $message"
    }
}

/**
 * Factory method to obtain a logger for this class.
 * @param privacyFlag Optional a [PrivacyFlag] to apply to all messages written to the
 * returned [Logger]
 * @return A lazily instantiated [Logger] instance.
 */
fun Any.logger(privacyFlag: PrivacyFlag? = null): Lazy<Logger> {
    return lazy { Logger.get(this::class, privacyFlag) }
}

/**
 * Factory method to obtain a logger for the given class.
 * @param klazz [KClass] instance
 * @param privacyFlag Optional a [PrivacyFlag] to apply to all messages written to the
 * returned [Logger]
 * @return A lazily instantiated [Logger] instance.
 */
fun <T : Any> logger(klazz: KClass<T>, privacyFlag: PrivacyFlag? = null): Lazy<Logger> {
    return lazy { Logger.get(klazz, privacyFlag) }
}


