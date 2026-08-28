package de.esolutions.connectedradioclient.helper

import de.esolutions.connectedradioclient.api.GenericMoshiParser
import de.esolutions.connectedradioclient.api.Http
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.backend.registration.result.ApiResultObject
import de.esolutions.connectedradioclient.data.backend.registration.result.ConfigObject
import de.esolutions.connectedradioclient.data.backend.registration.result.RegistrationResult
import de.esolutions.connectedradioclient.data.providers.DefaultTimeProvider
import de.esolutions.connectedradioclient.data.providers.TimeProvider
import de.esolutions.connectedradioclient.handlers.backend.middleware.HandleRegistration
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Assertions
import java.io.File
import java.lang.reflect.Modifier
import java.net.CookieManager
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

object TestHelpingFunctions {

    private val timeProvider: TimeProvider = DefaultTimeProvider()

    /**
     * Must be called before using a mock server, otherwise the requests will be sent in
     * encrypted form and the mock server does not support this.
     */
    fun setHttpToDefault() {
        val privateCookieManager = Http::class.java.getDeclaredField("cookieManager")
        privateCookieManager.isAccessible = true

        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true

        val modifier = ModifierAccessHacker.modifier
        modifier.isAccessible = true
        modifier.setInt(privateClient, privateClient.modifiers and Modifier.FINAL.inv())

        val client = OkHttpClient.Builder()
            .callTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(20L, TimeUnit.SECONDS)
            .writeTimeout(20L, TimeUnit.SECONDS)
            .connectTimeout(0L, TimeUnit.SECONDS)
            .cookieJar(JavaNetCookieJar(privateCookieManager.get(Http) as CookieManager))
            .build()

        privateClient.set(Http, client)
    }

    fun setupRequestConfig() {
        val workingDirectory = Paths.get("").toAbsolutePath().toString()
        Assertions.assertNotNull(workingDirectory)
        val testFilePath = "$workingDirectory/src/test/java/de/esolutions/connectedradioclient/testdata"
        val registrationJson = File("$testFilePath/registrationResponse.json").readText()
        val registrationResult = GenericMoshiParser.parseFromJson<RegistrationResult>(
            registrationJson
        )
        Assertions.assertNotNull(registrationResult)
        Config.requestConfig = registrationResult?.config!!
    }

    fun resetRequestConfig() {
        Config.requestConfig = ConfigObject(
            batchSize = 20,
            batchDebounce = 0.5,
            batchMaxWait = 2,
            connectionKeepalive = null,
            sessionEnabled = false,
            sessionCookieName = null,
            maxRegistrationAge = 86400L,
            retryDelay = 30L,
            statusCodeContentExpiry = mapOf(),
            statusCodeRetryBehavior = mapOf()
        )
    }

    fun removeAllCookies() {
        val privateCookieManager = Http::class.java.getDeclaredField("cookieManager")
        privateCookieManager.isAccessible = true
        val cookieManager = privateCookieManager.get(Http) as CookieManager
        cookieManager.cookieStore.removeAll()

        Assertions.assertEquals(0, cookieManager.cookieStore.cookies.size)
    }

    fun setDefaultConfig() {
        HandleRegistration.timeLastRegistrationRequest = timeProvider.getCurrentTimeMillis()
        Config.requestConfig = ConfigObject(
            5,
            0.5,
            2,
            300,
            true,
            "<not-set>",
            86400L,
            30L,
            mapOf(),
            mapOf()
        )
        Config.apiEndpoints = ApiResultObject(
            "/v2/lookup",
            "/v2/i/$(bearer)"
        )
    }

    fun clearRegistrationResult() {
        val api = Config::class.java.getDeclaredField("apiEndpoints")
        api.isAccessible = true
        api.set(null, null)

        Config.requestConfig = ConfigObject(
            20,
            0.5,
            2,
            300,
            false,
            null,
            86400L,
            30L,
            mapOf(),
            mapOf()
        )

        HandleRegistration.timeLastRegistrationRequest = null
    }
}
