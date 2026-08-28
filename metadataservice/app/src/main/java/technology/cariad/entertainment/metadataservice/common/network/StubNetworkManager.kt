package technology.cariad.entertainment.metadataservice.common.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * LOCAL BUILD STUB: NetworkManager that uses the real system network.
 * This bypasses the e.solutions framework ConnectivityManager but still uses
 * the real Android ConnectivityManager to get an actual Network object.
 */
class StubNetworkManager(private val context: Context) : INetworkManager {
    
    private val tag = "mds/StubNetworkManager"
    
    // Cache the flows per APN to maintain consistency
    private val networkInfoFlows = mutableMapOf<Pair<Apn, Boolean>, SharedFlow<NetworkInfo?>>()
    
    override fun getNetworkInfoFlow(apn: Apn, tethering: Boolean): SharedFlow<NetworkInfo?> {
        Log.i(tag, "getNetworkInfoFlow() - STUB: apn=$apn tethering=$tethering")
        
        return networkInfoFlows.getOrPut(apn to tethering) {
            var activeNetwork: Network? = null
            var interfaceName: String? = "wlan0"
            
            try {
                // Get real network from Android ConnectivityManager
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                activeNetwork = connectivityManager.activeNetwork
                
                // Try to get the actual interface name
                if (activeNetwork != null) {
                    try {
                        val linkProperties = connectivityManager.getLinkProperties(activeNetwork)
                        interfaceName = linkProperties?.interfaceName ?: "wlan0"
                        Log.i(tag, "Got real network: $activeNetwork, interface: $interfaceName")
                    } catch (e: Exception) {
                        Log.w(tag, "Could not get link properties: ${e.message}")
                    }
                } else {
                    Log.w(tag, "No active network available from ConnectivityManager!")
                }
            } catch (e: SecurityException) {
                Log.e(tag, "SecurityException getting network - missing ACCESS_NETWORK_STATE permission? ${e.message}")
            } catch (e: Exception) {
                Log.e(tag, "Error getting network: ${e.message}")
            }
            
            val networkInfo = NetworkInfo(
                apn = apn,
                network = activeNetwork,  // Use real Android Network object (may be null)
                interfaceName = interfaceName
            )
            
            Log.i(tag, "Creating NetworkInfo: apn=$apn, network=${activeNetwork}, interface=$interfaceName")
            
            // Return a StateFlow that always has the network available
            MutableStateFlow(networkInfo)
        }
    }
}

