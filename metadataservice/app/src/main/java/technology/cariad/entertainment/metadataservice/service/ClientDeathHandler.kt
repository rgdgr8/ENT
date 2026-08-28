package technology.cariad.entertainment.metadataservice.service

import android.os.IBinder
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.util.concurrent.ConcurrentHashMap

class ClientDeathHandler {
    private val log by logger()

    data class ClientInfo(
        val binder: IBinder,
        val deathRecipient: IBinder.DeathRecipient
    )

    private val deathRecipients: MutableMap<Int, ClientInfo> = ConcurrentHashMap()

    /**
     * Registers an [IBinder.DeathRecipient] for the passed [IBinder].
     * Upon receiving notice of the binder's death, the given action is invoked.
     * If a caller is no longer interested in the binder's death, they can [unregister] the handler.
     * In case a death notification for the binder is received, it is automatically unregistered.
     */
    internal fun register(clientId: Int, binder: IBinder, onDeath: () -> Unit) {
        log.info(this::register.name) { "register client id=$clientId" }

        val deathRecipient = IBinder.DeathRecipient {
            log.error("binderDied") { "client died id=$clientId" }

            unregister(clientId)
            onDeath()
        }
        binder.linkToDeath(deathRecipient, 0)
        deathRecipients[clientId] = ClientInfo(binder, deathRecipient)
    }

    /**
     * Removes a previously registered [IBinder.DeathRecipient].
     */
    internal fun unregister(clientId: Int) {
        log.info(this::unregister.name) { "unregister client id=$clientId" }

        deathRecipients.remove(clientId)?.let { info ->
            info.binder.unlinkToDeath(info.deathRecipient, 0)
        }
    }
}

