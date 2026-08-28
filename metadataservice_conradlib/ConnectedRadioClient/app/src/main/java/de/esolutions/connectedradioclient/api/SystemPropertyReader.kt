package de.esolutions.connectedradioclient.api

import android.annotation.SuppressLint
import de.esolutions.connectedradioclient.irc.IRC
import de.esolutions.connectedradioclient.irc.IRCEntry
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.PrivacyFlags

object SystemPropertyReader {

    private val systemPropertyCache = mutableMapOf<String, String?>()

    /**
     * Returns the ro.product.brand property from android.os.SystemProperties.
     */
    fun getBrand(): String {
        return getSystemPropertyCached("ro.product.brand") ?: "unknown"
    }

    /**
     * Returns the ro.build.product property from android.os.SystemProperties.
     */
    fun getProduct(): String {
        return getSystemPropertyCached("ro.build.product") ?: "unknown"
    }

    /**
     * Returns the ro.build.product property from android.os.SystemProperties.
     */
    fun getCluster(): String {
        return getSystemPropertyCached("ro.vendor.build.cluster") ?: "unknown"
    }

    /**
     * This method will cache the result of getSystemPropertyFromAndroid.
     */
    private fun getSystemPropertyCached(property: String): String? {
        if (systemPropertyCache.containsKey(property)) {
            return systemPropertyCache[property]
        }

        getSystemPropertyFromAndroid(property).let {
            systemPropertyCache[property] = it
            return it
        }
    }

    /**
     * This method will always return null in unit tests,
     * because android.os.SystemProperties is not available.
     */
    @SuppressLint("PrivateApi")
    private fun getSystemPropertyFromAndroid(property: String): String? {
        try {
            val systemProperties = Class.forName("android.os.SystemProperties")
            val getMethod = systemProperties.getMethod("get", String::class.java)
            val result = getMethod.invoke(systemProperties, property)?.toString()

            if (result == null || result.isEmpty()) {
                Logger.e(PrivacyFlags.SAFE, null, "System property '$property' is null or empty")
                return null
            }

            Logger.d(PrivacyFlags.SAFE, null, "Received system property '$property': '$result'")
            return result
        } catch (ex: Exception) {
            Logger.ex(PrivacyFlags.SAFE, null, "Can not get system property '$property'", ex)

            val ircMessage = "SystemProperties=${ex.javaClass.simpleName}-${ex.message}"
            IRC.error(IRCEntry.EXCEPTIONS, ircMessage)

            return null
        }
    }
}