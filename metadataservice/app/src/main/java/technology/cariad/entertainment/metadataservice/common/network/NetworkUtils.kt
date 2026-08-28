package technology.cariad.entertainment.metadataservice.common.network

import android.net.NetworkCapabilities
import android.net.NetworkRequest
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn

/**
 * Might be replaced with proper constant once a custom framework library for Android 11 including
 * system APIs is available.
 * @see [android.net.NetworkCapabilities.NET_CAPABILITY_OEM_PAID]
 */
private const val NET_CAPABILITY_OEM_PAID = 22

/**
 * Builds a network request with the given transport types and network capabilities.
 * @param transportTypes
 * @param capabilities
 * @return the network request
 */
fun buildNetworkRequest(
    transportTypes: List<Int>,
    capabilities: List<Int>
): NetworkRequest {
    return NetworkRequest.Builder().apply {
        transportTypes.forEach {
            addTransportType(it)
        }
        capabilities.forEach {
            addCapability(it)
        }
    }.build()
}

/**
 * Returns the transport types corresponding to the given apn and tethering flag.
 * @param apn
 * @param tethering
 * @return a list of transport types constants
 */
fun getTransportTypes(apn: Apn, tethering: Boolean): List<Int> {
    return mutableSetOf<Int>().apply {
        when (apn) {
            Apn.APN_NOT_ALLOWED -> return@apply
            Apn.APN_VENDOR, Apn.APN_CUSTOMER -> add(NetworkCapabilities.TRANSPORT_CELLULAR)
            Apn.APN_DLINK -> {
                add(NetworkCapabilities.TRANSPORT_WIFI)
                add(NetworkCapabilities.TRANSPORT_ETHERNET)
            }
        }
        if (tethering) {
            add(NetworkCapabilities.TRANSPORT_WIFI)
        }
    }.toList()
}

/**
 * Returns the network capabilities corresponding to the given apn.
 * @param apn
 * @return a list of network capability constants
 */
fun getNetworkCapabilities(apn: Apn): List<Int> {
    return mutableListOf<Int>().apply {
        when (apn) {
            Apn.APN_NOT_ALLOWED -> return@apply
            Apn.APN_VENDOR -> add(NET_CAPABILITY_OEM_PAID)
            Apn.APN_CUSTOMER -> add(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            Apn.APN_DLINK -> {
            }
        }
        add(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

