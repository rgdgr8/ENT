package technology.cariad.entertainment.metadataservice.common.serviceconnection

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlinx.coroutines.channels.ProducerScope

internal class ServiceConnectionCallback private constructor(
    channel: ProducerScope<IBinder?>
) : ServiceConnection, ProducerScope<IBinder?> by channel {

    private val log by logger()

    companion object {
        fun create(channel: ProducerScope<IBinder?>): ServiceConnectionCallback {
            return ServiceConnectionCallback(channel)
        }
    }

    /**
     * @see [ServiceConnection.onServiceConnected]
     */
    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        log.info(this::onServiceConnected.name) {
            "service connected ${name.packageName}/${name.className}"
        }

        trySend(binder)
    }

    /**
     * @see [ServiceConnection.onServiceDisconnected]
     */
    override fun onServiceDisconnected(name: ComponentName) {
        log.warn(this::onServiceDisconnected.name) {
            "service disconnected ${name.packageName}/${name.className}: " +
                    "waiting for service to reconnect (did the app crash?)"
        }

        trySend(null)
    }

    /**
     * @see [ServiceConnection.onBindingDied]
     */
    override fun onBindingDied(name: ComponentName) {
        log.error(this::onBindingDied.name) {
            "service binding died ${name.packageName}/${name.className}: " +
                    "attempting to rebind (update of app?)"
        }

        close(BindingDiedException())
    }

    /**
     * @see [ServiceConnection.onNullBinding]
     */
    override fun onNullBinding(name: ComponentName) {
        log.fatal(this::onNullBinding.name) {
            "binding rejected by service ${name.packageName}/${name.className}: " +
                    "service not usable"
        }

        close(NullBindingException())
    }
}

