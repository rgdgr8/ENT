package technology.cariad.entertainment.metadataservice.common.network

import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.NO_NETWORK_AVAILABLE_ERROR
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.servicelist.Apn
import kotlinx.coroutines.channels.ProducerScope

internal class NetworkCallback private constructor(
    private val apn: Apn,
    private val connectivityManager: ConnectivityManager,
    channel: ProducerScope<NetworkInfo?>,
) : ConnectivityManager.NetworkCallback(), ProducerScope<NetworkInfo?> by channel {

    companion object {
        fun create(
            apn: Apn,
            connectivityManager: ConnectivityManager,
            channel: ProducerScope<NetworkInfo?>
        ): NetworkCallback {
            return NetworkCallback(apn, connectivityManager, channel)
        }
    }

    private val log by logger()

    private var interfaceName: String? = null

    override fun onAvailable(network: Network) {
        log.info("onAvailable") {
            "new network available for apn=$apn: network=$network"
        }
        connectivityManager.bindProcessToNetwork(network)

        interfaceName = null
    }

    override fun onCapabilitiesChanged(
        network: Network,
        networkCapabilities: NetworkCapabilities
    ) {
        log.info(this::onCapabilitiesChanged.name) {
            "network capabilities changed for apn=$apn network=$network: " +
                    "capabilities=$networkCapabilities"
        }
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
        interfaceName = linkProperties.interfaceName

        log.info(this::onLinkPropertiesChanged.name) {
            "link properties changed for apn=$apn network=$network: " +
                    "interfaceName=$interfaceName"
        }

        interfaceName?.let {
            trySend(NetworkInfo(apn, network, it))
        }
    }

    override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
        log.info(this::onBlockedStatusChanged.name) {
            "blocked status changed for apn=$apn network=$network: blocked=$blocked"
        }

        if (blocked) {
            trySend(null)
        } else {
            interfaceName?.let {
                trySend(NetworkInfo(apn, network, it))
            }
        }
    }

    override fun onLost(network: Network) {
        log.info(this::onLost.name) {
            "network lost for apn=$apn: network=$network"
        }

        trySend(null)
    }

    override fun onUnavailable() {
        log.error(this::onUnavailable.name) {
            "no network available for apn=$apn"
        }
        IRCLogger.appError(NO_NETWORK_AVAILABLE_ERROR)
        close(NetworkUnavailableException("network unavailable for apn=$apn"))
    }
}

