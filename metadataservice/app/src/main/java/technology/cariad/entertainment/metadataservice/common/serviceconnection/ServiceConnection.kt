package technology.cariad.entertainment.metadataservice.common.serviceconnection

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.serviceresolver.IServiceResolver
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retryWhen

class ServiceConnection(
    private val serviceResolver: IServiceResolver,
    private val context: Context,
    private val action: String
) {
    private val log by logger()

    /**
     * Converts a service connection and the respective callbacks into a cold flow.
     * The service connection is established upon flow collection and closed when the flow
     * collection is stopped.
     * The flow emits the [IBinder] object for RPC communication when the remote service was
     * successfully bound.
     * If the remote service crashes, the binding is automatically reestablished.
     * If an unrecoverable error occurs, e.g. the remote service doesn't exist, the flow is closed.
     */
    fun serviceConnectionFlow() = callbackFlow {
        val serviceConnection = ServiceConnectionCallback.create(this)

        val intent = serviceResolver.resolve(action)
        val isBound = if (intent != null) {
            bindService(serviceConnection, intent)
        } else false

        if (!isBound) {
            close(ServiceUnavailableException("service unavailable or missing permission"))
        }

        awaitClose {
            if (isBound) {
                unbindService(serviceConnection)
            }
        }
    }.retryWhen { cause, _ ->
        cause is BindingDiedException
    }

    private fun bindService(connection: ServiceConnection, intent: Intent): Boolean {
        log.info(this::bindService.name) {
            "attempting to bind service intent=$intent"
        }

        return try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE).also { success ->
                if (!success) {
                    log.error(this::bindService.name) {
                        "failed to bind service intent=$intent: " +
                                "(app not installed or missing permission?)"
                    }
                }
            }
        } catch (e: SecurityException) {
            log.error(this::bindService.name) {
                "failed to bind service intent=$intent: " +
                        "SecurityException=${e.message} (app updated security policy?)"
            }
            false
        }
    }

    private fun unbindService(connection: ServiceConnection) {
        log.info(this::unbindService.name) {
            "unbind from service action=$action"
        }

        context.unbindService(connection)
    }
}

