package de.esolutions.connectedradioclient.api

import android.net.Network
import de.esolutions.connectedradioclient.data.REQUEST_ID
import de.esolutions.connectedradioclient.data.RequestHeader
import de.esolutions.connectedradioclient.helper.TestHelpingFunctions
import de.esolutions.connectedradioclient.helper.TestHttpCallback
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory
import okhttp3.internal.connection.RealConnectionPool
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class HttpTest {

    private val mockServer: MockWebServer = MockWebServer()
    private lateinit var url: HttpUrl
    private val headers = RequestHeader("token", "requestID")

    @BeforeEach
    fun before() {
        TestHelpingFunctions.setHttpToDefault()
        mockServer.start()
        url = mockServer.url("")
        headers.mcc = "mcc"
        headers.accept = "accept"
    }

    @AfterEach
    fun after() {
        mockServer.shutdown()
        TestHelpingFunctions.setHttpToDefault()
    }

    @Test
    fun enqueue_get_correct() {
        Http.enqueueGet(url, headers, TestHttpCallback())
        val request = mockServer.takeRequest(2, TimeUnit.SECONDS)
        val headers = request?.headers!!
        val body = request.body.readUtf8()

        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("requestID", headers[REQUEST_ID])
        assertEquals("mcc", headers["X-Mcc"])
        assertEquals("accept", headers["Accept"])

        assertEquals("", body)
    }

    @Test
    fun execute_get_correct_and_return_correct_response() {
        mockServer.enqueue(MockResponse().setBody("Some Nice Body"))

        val response = Http.executeGet(url, headers)
        val request = mockServer.takeRequest(2, TimeUnit.SECONDS)
        val headers = request?.headers!!
        val body = request.body.readUtf8()

        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("requestID", headers[REQUEST_ID])
        assertEquals("mcc", headers["X-Mcc"])
        assertEquals("accept", headers["Accept"])

        assertEquals("", body)

        assertNotNull(response)
        val responseBody = response.body?.string()
        assertEquals("Some Nice Body", responseBody)
    }

    @Test
    fun execute_get_correct_but_run_in_timeout_and_throw_exception() {
        // No responses scheduled => Simulated time out
        assertThrows(IOException::class.java) {
            Http.executeGet(url, headers)
        }

        // Test if the request was sent correctly
        val request = mockServer.takeRequest(2, TimeUnit.SECONDS)
        val headers = request?.headers!!
        val body = request.body.readUtf8()

        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("requestID", headers[REQUEST_ID])
        assertEquals("mcc", headers["X-Mcc"])
        assertEquals("accept", headers["Accept"])

        assertEquals("", body)
    }

    @Test
    fun enqueue_post_correct() {
        Http.enqueuePost(url, headers, "bodyValue", TestHttpCallback())

        val request = mockServer.takeRequest(2, TimeUnit.SECONDS)
        val headers = request?.headers!!
        val body = request.body.readUtf8()

        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("requestID", headers[REQUEST_ID])
        assertEquals("mcc", headers["X-Mcc"])
        assertEquals("accept", headers["Accept"])

        assertEquals("bodyValue", body)
    }

    @Test
    fun execute_post_correct_and_return_correct_response() {
        mockServer.enqueue(MockResponse().setBody("Some Nice Body"))

        val response = Http.executePost(url, headers, "bodyValue")
        val request = mockServer.takeRequest(2, TimeUnit.SECONDS)
        val headers = request?.headers!!
        val body = request.body.readUtf8()

        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("requestID", headers[REQUEST_ID])
        assertEquals("mcc", headers["X-Mcc"])
        assertEquals("accept", headers["Accept"])

        assertNotNull(body)
        assertEquals("bodyValue", body)

        assertNotNull(response)
        val responseBody = response.body?.string()
        assertEquals("Some Nice Body", responseBody)
    }

    @Test
    fun execute_post_correct_but_run_in_timeout_and_throw_exception() {
        // No responses scheduled => Simulated time out
        assertThrows(IOException::class.java) {
            Http.executePost(url, headers, "bodyValue")
        }

        // Test if the request was sent correctly.
        val request = mockServer.takeRequest(2, TimeUnit.SECONDS)
        val headers = request?.headers!!
        val body = request.body.readUtf8()

        assertEquals("Bearer token", headers["Authorization"])
        assertEquals("requestID", headers[REQUEST_ID])
        assertEquals("mcc", headers["X-Mcc"])
        assertEquals("accept", headers["Accept"])

        assertEquals("bodyValue", body)
    }

    @Test
    fun return_null_without_stored_cookie() {
        val cookie = Http.getCookie("anyName")

        assertNull(cookie)
    }

    @Test
    fun return_the_stored_cookie() {
        val privateCookieManager = Http::class.java.getDeclaredField("cookieManager")
        privateCookieManager.isAccessible = true
        val cookieManager = privateCookieManager.get(Http) as CookieManager
        cookieManager.cookieStore.add(URI(""), HttpCookie("testCookie", "42"))

        val cookie = Http.getCookie("testCookie")
        assertNotNull(cookie)
        assertEquals("42", cookie!!.value)
    }

    @Test
    fun use_the_same_stored_cookie_after_network_set() {
        val privateCookieManager = Http::class.java.getDeclaredField("cookieManager")
        privateCookieManager.isAccessible = true
        val cookieManager = privateCookieManager.get(Http) as CookieManager
        cookieManager.cookieStore.add(URI(""), HttpCookie("testCookie", "42"))

        val cookieBefore = Http.getCookie("testCookie")
        assertNotNull(cookieBefore)
        assertEquals("42", cookieBefore!!.value)

        val mockNetwork = mock(Network::class.java)
        val mockSocketFactory = mock(SocketFactory::class.java)
        doAnswer { mockSocketFactory }.`when`(mockNetwork).socketFactory
        Http.setNetwork(mockNetwork)

        val cookieAfter = Http.getCookie("testCookie")
        assertNotNull(cookieAfter)
        assertEquals("42", cookieAfter!!.value)
    }

    @Test
    fun set_the_network_correct() {
        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true
        val clientBefore = privateClient.get(Http) as OkHttpClient
        val cookieJarBefore = clientBefore.cookieJar

        assertEquals(SocketFactory.getDefault(), clientBefore.socketFactory)
        //the default DNS is DNS.SYSTEM from the okhttp3 but it is not available in this class
        assertTrue(clientBefore.dns !is NetworkDns)

        val mockNetwork = mock(Network::class.java)
        val mockSocketFactory = mock(SocketFactory::class.java)
        doAnswer { mockSocketFactory }.`when`(mockNetwork).socketFactory
        Http.setNetwork(mockNetwork)
        val clientAfter = privateClient.get(Http) as OkHttpClient
        val cookieJarAfter = clientAfter.cookieJar

        assertEquals(mockSocketFactory, clientAfter.socketFactory)
        assertTrue(clientAfter.dns is NetworkDns)
        assertEquals(cookieJarBefore, cookieJarAfter)
    }

    @Test
    @DisplayName("it_creates_the_request_builder_with_headers_and_the_correct_url[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-244019#]")
    fun it_creates_the_request_builder_with_headers_and_the_correct_url() {
        val builder = Http.createRequestBuilderWithHeaders("http://domain/".toHttpUrl(), headers)
        val request = builder.build()

        assertEquals("http://domain/", request.url.toString())
        assertEquals("unknown", request.header("X-BRAND"))
        assertEquals("unknown", request.header("X-CLUSTER"))
        assertEquals("unknown", request.header("X-PRODUCT"))
    }

    @Test
    @DisplayName("it_resets_all_connections_at_keep_alive_change[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253991#]")
    fun it_resets_all_connections_at_keep_alive_change() {
        val mockConnectionPool = mock(ConnectionPool::class.java)

        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true
        val mockClient = OkHttpClient.Builder().connectionPool(mockConnectionPool).build()
        privateClient.set(Http, mockClient)

        Http.setKeepAlive(1)

        verify(mockConnectionPool).evictAll()
    }

    @Test
    @DisplayName("it_resets_all_connections_at_network_change[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253991#]")
    fun it_resets_all_connections_at_network_change() {
        val mockConnectionPool = mock(ConnectionPool::class.java)

        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true
        val mockClient = OkHttpClient.Builder().connectionPool(mockConnectionPool).build()
        privateClient.set(Http, mockClient)

        val mockNetwork = mock(Network::class.java)
        val mockSocketFactory = mock(SocketFactory::class.java)
        doAnswer { mockSocketFactory }.`when`(mockNetwork).socketFactory

        Http.setNetwork(mockNetwork)

        verify(mockConnectionPool).evictAll()
    }

    @Test
    @DisplayName("it_sets_a_positive_keep_alive_value[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253991#]")
    fun it_sets_a_positive_keep_alive_value() {
        Http.setKeepAlive(123)

        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true
        val client = privateClient.get(Http) as OkHttpClient
        val connectionPool = client.connectionPool

        val privateRealPool = ConnectionPool::class.java.getDeclaredField("delegate")
        privateRealPool.isAccessible = true
        val realPool = privateRealPool.get(connectionPool) as RealConnectionPool

        val privateMaxIdleConnections =
            RealConnectionPool::class.java.getDeclaredField("maxIdleConnections")
        privateMaxIdleConnections.isAccessible = true
        val maxIdleConnections = privateMaxIdleConnections.get(realPool) as Int
        assertEquals(5, maxIdleConnections)

        val privateKeepAliveDurationNs =
            RealConnectionPool::class.java.getDeclaredField("keepAliveDurationNs")
        privateKeepAliveDurationNs.isAccessible = true
        val keepAliveDurationNs = privateKeepAliveDurationNs.get(realPool) as Long
        assertEquals(123_000_000_000L, keepAliveDurationNs)
    }

    @Test
    @DisplayName("it_disables_keep_alive_if_value_is_zero[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-253991#]")
    fun it_disables_keep_alive_if_value_is_zero() {
        Http.setKeepAlive(0)

        val privateClient = Http::class.java.getDeclaredField("client")
        privateClient.isAccessible = true
        val client = privateClient.get(Http) as OkHttpClient
        val connectionPool = client.connectionPool

        val privateRealPool = ConnectionPool::class.java.getDeclaredField("delegate")
        privateRealPool.isAccessible = true
        val realPool = privateRealPool.get(connectionPool) as RealConnectionPool

        val privateMaxIdleConnections =
            RealConnectionPool::class.java.getDeclaredField("maxIdleConnections")
        privateMaxIdleConnections.isAccessible = true
        val maxIdleConnections = privateMaxIdleConnections.get(realPool) as Int
        assertEquals(0, maxIdleConnections)

        val privateKeepAliveDurationNs =
            RealConnectionPool::class.java.getDeclaredField("keepAliveDurationNs")
        privateKeepAliveDurationNs.isAccessible = true
        val keepAliveDurationNs = privateKeepAliveDurationNs.get(realPool) as Long
        assertEquals(1L, keepAliveDurationNs)
    }

    @Test
    @DisplayName("only_specified_cipher_suites_are_in_the_connection_spec[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-281626,OI_Global_CLU51:OIG-53049,OI_Global_CLU51:OIG-93140#]")
    fun only_specified_cipher_suites_are_in_the_connection_spec() {
        val actualCipherSuites = Http.getConnectionSpec().cipherSuites!!

        assertEquals(10, actualCipherSuites.size)

        // Cipher Suites for TLS 1.2 and 1.3
        assertFalse(actualCipherSuites.contains(CipherSuite.TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256))
        assertFalse(actualCipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256))

        // Cipher Suites for TLS 1.3 only
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_AES_256_GCM_SHA384))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_CHACHA20_POLY1305_SHA256))
        assertTrue(actualCipherSuites.contains(CipherSuite.TLS_AES_128_GCM_SHA256))
    }

    @Test
    @DisplayName("only_specified_tls_versions_are_in_the_connection_spec[#LinkedWi:HCP3_GLOBAL_CLU41:HCPG-281626,OI_Global_CLU51:OIG-53049#]")
    fun only_specified_tls_versions_are_in_the_connection_spec() {
        val actualTlsVersion = Http.getConnectionSpec().tlsVersions!!

        assertEquals(2, actualTlsVersion.size)

        assertTrue(actualTlsVersion.contains(TlsVersion.TLS_1_2))
        assertTrue(actualTlsVersion.contains(TlsVersion.TLS_1_3))
    }

}