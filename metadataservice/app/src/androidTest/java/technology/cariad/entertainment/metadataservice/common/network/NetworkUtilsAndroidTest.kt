package technology.cariad.entertainment.metadataservice.common.network

import android.net.NetworkCapabilities
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkUtilsAndroidTest {

    @Test
    fun buildNetworkRequest_shouldSetCapabilitiesAndTransportType() {
        val request = buildNetworkRequest(
            listOf(NetworkCapabilities.TRANSPORT_WIFI),
            listOf(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        )

        Assert.assertTrue(request.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        Assert.assertTrue(request.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
    }
}

