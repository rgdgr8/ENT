package de.esolutions.connectedradioclient.api

import android.net.Network
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterEach
import org.mockito.Mockito
import java.net.InetAddress
import java.net.UnknownHostException

class NetworkDnsTest {

    companion object {

        var mockNetwork: Network? = null
        var mockInetAddress: InetAddress? = null

        @BeforeAll
        @JvmStatic
        fun setUp() {
            mockNetwork = Mockito.mock(Network::class.java)
            mockInetAddress = Mockito.mock(InetAddress::class.java)
        }
    }

    @AfterEach
    fun reset() {
        Mockito.reset(mockNetwork, mockInetAddress)
    }

    @Test
    fun test_lookup_network_not_null_host_found() {
        val host = arrayOf(mockInetAddress)
        Mockito.`when`(
            mockNetwork?.getAllByName(
                Mockito.any(String::class.java)
            )
        ).thenReturn(host)

        val dns = NetworkDns(mockNetwork)
        Assertions.assertEquals(host.toList(), dns.lookup("MyTestHost"))
    }

    @Test
    fun test_lookup_network_not_null_host_not_found_unknown_host_exception() {
        Mockito.`when`(
            mockNetwork?.getAllByName(
                Mockito.any(String::class.java)
            )
        ).thenReturn(null)

        val dns = NetworkDns(mockNetwork)
        Assertions.assertThrows(UnknownHostException::class.java) {
            dns.lookup("MyTestHost")
        }
    }

    @Test
    fun test_lookup_network_null_unknown_host_exception() {
        val dns = NetworkDns(null)
        Assertions.assertThrows(UnknownHostException::class.java) {
            dns.lookup("MyTestHost")
        }
    }
}