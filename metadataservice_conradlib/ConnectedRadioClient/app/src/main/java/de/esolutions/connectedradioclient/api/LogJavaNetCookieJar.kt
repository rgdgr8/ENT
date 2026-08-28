package de.esolutions.connectedradioclient.api

import androidx.annotation.VisibleForTesting
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.net.CookieHandler
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.JavaNetCookieJar

/**
 * This class is a wrapper around JavaNetCookieJar.
 * The saveFromResponse function will additionally log new cookie values.
 */
class LogJavaNetCookieJar(cookieHandler: CookieHandler) : CookieJar {

    private val jar = JavaNetCookieJar(cookieHandler)

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return jar.loadForRequest(url)
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val cookiesBefore = loadForRequest(url)

        jar.saveFromResponse(url, cookies)

        cookies.forEach { newCookie ->
            val oldCookie = findCookie(newCookie.name, cookiesBefore)

            if (shouldLogCookieChange(newCookie, oldCookie)) {
                logCookie(newCookie, oldCookie)
            }
        }
    }

    /**
     * Finds a cookie from a list by the name.
     */
    @VisibleForTesting
    fun findCookie(name: String, cookies: List<Cookie>): Cookie? {
        return cookies.find { it.name == name }
    }

    /**
     * This class should log a cookie change if a new cookie got set or the value of a cookie got changed.
     */
    @VisibleForTesting
    fun shouldLogCookieChange(newCookie: Cookie, oldCookie: Cookie?): Boolean {
        return oldCookie == null || oldCookie.value != newCookie.value
    }

    private fun logCookie(newCookie: Cookie, oldCookie: Cookie?) {
        val expiresAt = convertTimestampToString(newCookie.expiresAt)

        val valueMessage = "'${oldCookie?.value}' -> '${newCookie.value}'"
        val logMessage = "Updated Cookie '${newCookie.name}': $valueMessage (Expires at $expiresAt)"

        Logger.w(PrivacyFlags.SESSION, null, logMessage, LoggerTags.RESPONSE, LoggerTags.COOKIE)
    }

    private fun convertTimestampToString(timestamp: Long): String {
        return DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss z")
            .withZone(ZoneOffset.systemDefault())
            .format(Instant.ofEpochMilli(timestamp))
    }
}