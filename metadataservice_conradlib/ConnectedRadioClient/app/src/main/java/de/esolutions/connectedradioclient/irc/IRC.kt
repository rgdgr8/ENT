package de.esolutions.connectedradioclient.irc

import de.esolutions.connectedradioclient.BuildConfig
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.PrivacyFlags

/**
 * No-Op implementation of IRC (Incident Recording Component).
 * The ESO framework IRC is not available, so all methods are stubs.
 */
object IRC {

    /**
     * Writes the current client version to the IRC.
     * No-Op: Just logs locally instead.
     */
    fun printVersionInformation() {
        Logger.i(PrivacyFlags.SAFE, null, "Version: ${BuildConfig.VERSION_NAME}")
    }

    /**
     * Logs the message id to the specified channel as a system message.
     * No-Op: Just logs locally instead.
     */
    fun error(entry: IRCEntry, message: IRCMessage) {
        Logger.e(PrivacyFlags.SAFE, null, "IRC[${entry.entry}]: ${message.id}")
    }

    /**
     * Logs the http status code to the specified channel as a system message.
     * No-Op: Just logs locally instead.
     */
    fun error(entry: IRCEntry, httpCode: Int) {
        Logger.e(PrivacyFlags.SAFE, null, "IRC[${entry.entry}]: HTTP $httpCode")
    }

    /**
     * Logs the message to the specified channel as a system message.
     * No-Op: Just logs locally instead.
     */
    fun error(entry: IRCEntry, message: String) {
        val messageWithMaxLength = message.take(128)
        Logger.e(PrivacyFlags.SAFE, null, "IRC[${entry.entry}]: $messageWithMaxLength")
    }
}