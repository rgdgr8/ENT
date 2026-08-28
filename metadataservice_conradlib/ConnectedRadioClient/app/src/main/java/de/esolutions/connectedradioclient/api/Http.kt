package de.esolutions.connectedradioclient.api

import android.net.Network
import androidx.annotation.VisibleForTesting
import de.esolutions.connectedradioclient.data.Config
import de.esolutions.connectedradioclient.data.RequestHeader
import de.esolutions.connectedradioclient.logger.LogInfo
import de.esolutions.connectedradioclient.logger.Logger
import de.esolutions.connectedradioclient.logger.LoggerTags
import de.esolutions.connectedradioclient.logger.PrivacyFlags
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpCookie
import java.util.Collections
import java.util.UUID
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Callback
import okhttp3.CipherSuite
import okhttp3.ConnectionPool
import okhttp3.ConnectionSpec
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.TlsVersion

object Http {

    private const val CALL_TIMEOUT = 40L
    private const val READ_TIMEOUT = 20L
    private const val WRITE_TIMEOUT = 20L
    private const val CONNECT_TIMEOUT = 10L

    private val cookieManager = CookieManager()
    private var client: OkHttpClient

    init {
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

        client = OkHttpClient.Builder()
            .callTimeout(CALL_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .cookieJar(LogJavaNetCookieJar(cookieManager))
            .connectionSpecs(Collections.singletonList(getConnectionSpec()))
            .build()
    }

    /**
     * Backend only accepts TSL 1.2 and above (2021-09-23).
     * This Cypher suite configuration was instructed by stbr.
     *
     * See 080_220_LAH.DUM.900.AF_Transport_Layer_Security_EN_DE_V1.9
     * https://polarion-hcp3.eso.local/polarion/#/project/OI_Global_Sync/workitem?id=OI_Sync-8495
     */
    @VisibleForTesting
    fun getConnectionSpec(): ConnectionSpec {
        return ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_AES_256_GCM_SHA384,
                CipherSuite.TLS_CHACHA20_POLY1305_SHA256,
                CipherSuite.TLS_AES_128_GCM_SHA256
            ).build()
    }

    /**
     * Enqueue asynchronous GET request, the response will be available in the callback.
     */
    fun enqueueGet(url: HttpUrl, header: RequestHeader, callback: Callback) {
        Logger.d(
            PrivacyFlags.UNDECIDED,
            LogInfo(null, header.requestID),
            "Enqueue GET request to $url",
            LoggerTags.REQUEST
        )
        createGETCall(url, header).enqueue(callback)
    }

    /**
     * Executes synchronous GET request and returns response or throws exception.
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    fun executeGet(url: HttpUrl, header: RequestHeader): Response {
        Logger.d(
            PrivacyFlags.UNDECIDED,
            LogInfo(null, header.requestID),
            "Execute GET request to $url",
            LoggerTags.REQUEST
        )
        return createGETCall(url, header).execute()
    }

    /**
     * Enqueue asynchronous POST request, the response will be available in the callback.
     */
    fun enqueuePost(url: HttpUrl, header: RequestHeader, body: String, callback: Callback) {
        Logger.d(
            PrivacyFlags.UNDECIDED,
            LogInfo(null, header.requestID),
            "Enqueue POST request to $url with $body",
            LoggerTags.REQUEST
        )
        createPOSTCall(url, header, body).enqueue(callback)
    }

    /**
     * Executes synchronous POST request and returns response or throws exception.
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    fun executePost(url: HttpUrl, header: RequestHeader, body: String): Response {
        Logger.d(
            PrivacyFlags.UNDECIDED,
            LogInfo(null, header.requestID),
            "Execute POST request to $url with $body",
            LoggerTags.REQUEST
        )
        return createPOSTCall(url, header, body).execute()
    }

    /**
     * Builds an OkHttp request object by the given url and headers.
     */
    @VisibleForTesting
    fun createRequestBuilderWithHeaders(url: HttpUrl, headers: RequestHeader): Request.Builder {
        return Request.Builder().url(url).headers(headers.toHeaders())
    }

    private fun createGETCall(url: HttpUrl, header: RequestHeader): Call {
        val requestBuilder = this.createRequestBuilderWithHeaders(url, header)
        val request = requestBuilder.get().build()

        logRequest(request, header.requestID)
        return client.newCall(request)
    }

    private fun createPOSTCall(url: HttpUrl, header: RequestHeader, body: String): Call {
        val requestBuilder = this.createRequestBuilderWithHeaders(url, header)
        val requestBody = body.toRequestBody("application/json".toMediaTypeOrNull())
        val request = requestBuilder.post(requestBody).build()

        logRequest(request, header.requestID)
        return client.newCall(request)
    }

    private fun logRequest(request: Request, requestID: String) {
        val headerMapWithoutAuth = request.headers.toMap().filterKeys { it != "Authorization" }
        val cookies = cookieManager.cookieStore.cookies.filter {
            it.name != Config.requestConfig.sessionCookieName
        }

        Logger.i(
            PrivacyFlags.UNDECIDED,
            LogInfo(null, requestID),
            "Create request to ${request.url} with headers $headerMapWithoutAuth and cookies $cookies",
            LoggerTags.REQUEST
        )
    }

    /**
     * return the [HttpCookie] object or null for the given [name]
     */
    fun getCookie(name: String): HttpCookie? {
        return cookieManager.cookieStore.cookies.find { it.name == name }
    }

    /**
     * Generate a UUID and return the value as String
     */
    fun generateRequestID(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Set the keep alive for the okhttp connection to the backend.
     * Use the given [keepAlive] for the time and set max connection to 5 (default value).
     * If the keep alive should be disabled (value of zero or below), then set the maxIdleConnections
     * to zero, so OkHttp will close the connection after every request.
     */
    fun setKeepAlive(keepAlive: Long) {
        val connectionPool = if (keepAlive > 0) {
            ConnectionPool(5, keepAlive, TimeUnit.SECONDS)
        } else {
            ConnectionPool(0, 1L, TimeUnit.NANOSECONDS)
        }

        client.connectionPool.evictAll()

        client = client.newBuilder()
            .connectionPool(connectionPool)
            .build()
    }

    /**
     * Set the given [network] object to the OkHttp
     */
    fun setNetwork(network: Network) {
        client.connectionPool.evictAll()

        client = client.newBuilder()
            .socketFactory(network.socketFactory)
            .dns(NetworkDns(network))
            .build()
    }
}