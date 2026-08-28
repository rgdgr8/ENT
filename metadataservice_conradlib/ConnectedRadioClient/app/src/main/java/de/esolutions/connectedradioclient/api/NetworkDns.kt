package de.esolutions.connectedradioclient.api

import android.net.Network
import java.net.InetAddress
import okhttp3.Dns

/**
 * This class configure the DNS that the DNS request also goes over the given [mNetwork].
 * Code is from the Architecture Documentation
 * "Android ConnectivityManager for HCP3 – APN management" Version 0.5
 */
class NetworkDns(private var mNetwork: Network? = null) : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        return mNetwork?.getAllByName(hostname)?.toList() ?: Dns.SYSTEM.lookup(hostname)
    }
}