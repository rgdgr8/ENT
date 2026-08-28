package technology.cariad.entertainment.metadataservice.common.network

import android.net.NetworkCapabilities
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import org.junit.Assert
import org.junit.Test

class NetworkUtilsTest {

    @Test
    fun getTransportTypes_shouldIncludeCellularForApnVendorOrCustomer() {
        val transportTypesCustomer = getTransportTypes(Apn.APN_CUSTOMER, false)
        Assert.assertEquals(transportTypesCustomer.size, 1)
        Assert.assertTrue(transportTypesCustomer.contains(NetworkCapabilities.TRANSPORT_CELLULAR))

        val transportTypesVendor = getTransportTypes(Apn.APN_VENDOR, false)
        Assert.assertEquals(transportTypesVendor.size, 1)
        Assert.assertTrue(transportTypesVendor.contains(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    @Test
    fun getTransportTypes_shouldIncludeWifiAndEthernetForApnDLink() {
        val transportTypes = getTransportTypes(Apn.APN_DLINK, false)
        Assert.assertEquals(transportTypes.size, 2)
        Assert.assertTrue(
            transportTypes.containsAll(
                setOf(
                    NetworkCapabilities.TRANSPORT_ETHERNET,
                    NetworkCapabilities.TRANSPORT_WIFI
                )
            )
        )
    }

    @Test
    fun getTransportTypes_shouldIncludeWifiAAnyApnWithTethering() {
        val transportTypes = getTransportTypes(Apn.APN_VENDOR, true)
        Assert.assertTrue(
            transportTypes.containsAll(
                setOf(
                    NetworkCapabilities.TRANSPORT_WIFI,
                )
            )
        )
    }

    @Test
    fun getTransportTypes_shouldReturnEmptyListForApnNotAllowed() {
        val transportTypes = getTransportTypes(Apn.APN_NOT_ALLOWED, false)
        Assert.assertTrue(transportTypes.isEmpty())

        val transportTypesTethering = getTransportTypes(Apn.APN_NOT_ALLOWED, true)
        Assert.assertTrue(transportTypesTethering.isEmpty())
    }

    @Test
    fun getNetworkCapabilities_shouldIncludeOemPaidForApnVendor() {
        val networkCapabilities = getNetworkCapabilities(Apn.APN_VENDOR)
        Assert.assertTrue(networkCapabilities.contains(22 /*OEM_PAID*/))
    }

    @Test
    fun getNetworkCapabilities_shouldIncludeNotRestrictedForApnCustomer() {
        val networkCapabilities = getNetworkCapabilities(Apn.APN_CUSTOMER)
        Assert.assertTrue(
            networkCapabilities.contains(
                NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED
            )
        )
    }

    @Test
    fun getNetworkCapabilities_shouldIncludeOnlyInternetForApnDLink() {
        val networkCapabilities = getNetworkCapabilities(Apn.APN_DLINK)
        Assert.assertEquals(
            networkCapabilities,
            listOf(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        )
    }

    @Test
    fun getNetworkCapabilities_shouldIncludeInternetForAnyApn() {
        val networkCapabilities = getNetworkCapabilities(Apn.APN_VENDOR)
        Assert.assertTrue(networkCapabilities.contains(NetworkCapabilities.NET_CAPABILITY_INTERNET))
    }

    @Test
    fun getNetworkCapabilities_shouldReturnEmptyListForNotAllowed() {
        val networkCapabilitiesNotAllowed = getNetworkCapabilities(Apn.APN_NOT_ALLOWED)
        Assert.assertTrue(networkCapabilitiesNotAllowed.isEmpty())
    }
}

