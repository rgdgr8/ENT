package technology.cariad.entertainment.metadataservice.service

import android.os.Binder
import android.os.IBinder
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceManagement
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap

class MetadataServiceManagementImpl : IMetadataServiceManagement.Stub() {

    private val log by logger()

    private val requests: MutableMap<Pair<Int, Int>, Job> = ConcurrentHashMap()
    private val clientManager = ClientManager()
    private val clientDeathHandler = ClientDeathHandler()

    /**
     * @see [IMetadataServiceManagement.registerClient]
     */
    override fun registerClient(clientName: String, binder: IBinder): Int {
        val clientUid = Binder.getCallingUid()
        val clientId = clientManager.register(clientName, clientUid)

        return registerClientWithIds(clientName, binder, clientId, clientUid)
    }

    override fun registerClientWithIds(
        clientName: String, binder: IBinder, clientId: Int, clientUid: Int
    ): Int {
        clientDeathHandler.register(clientId, binder) {
            log.error("binderDied") {
                "remove dead client id=$clientId name=$clientName (uid=$clientUid)"
            }
            IRCLogger.appGenericError(
                "binderDied",
                "remove dead client id=$clientId name=$clientName (uid=$clientUid)"
            )
            removeClient(clientId, clientUid)
        }

        log.info(this::registerClient.name) {
            "assigned id=$clientId to client=$clientName (uid=$clientUid)"
        }

        return clientId
    }

    /**
     * @see [IMetadataServiceManagement.unregisterClient]
     */
    override fun unregisterClient(clientId: Int, clientName: String) {
        val uid = Binder.getCallingUid()

        log.info(this::unregisterClient.name) {
            "remove client id=$clientId name=$clientName (uid=$uid)"
        }

        clientDeathHandler.unregister(clientId)
        removeClient(clientId, uid)
    }

    /**
     * Checks if a client with the given [clientId] and [userId] is registered.
     * @return true if a client is registered, false otherwise
     */
    fun isRegistered(clientId: Int, userId: Int) = clientManager.isRegistered(clientId, userId)

    /**
     * Removes a client and cancels all running requests.
     */
    fun removeClient(clientId: Int, userId: Int) {
        val success = clientManager.unregister(clientId, userId)

        val canceled = mutableListOf<Job>()
        requests.keys.filter { key -> key.first == clientId }
            .mapNotNull { key -> requests.remove(key) }
            .forEach { job ->
                job.cancel()
                canceled.add(job)
            }

        runBlocking {
            canceled.forEach { it.join() }
        }

        if (success) {
            log.info(this::removeClient.name) {
                "successfully removed client: id=$clientId (uid=$userId)"
            }

        } else {
            log.warn(this::removeClient.name) {
                "failed to remove unknown client: id=$clientId (uid=$userId)"
            }
        }
    }

    /**
     * Adds a new request for the given client.
     */
    fun addRequest(clientId: Int, requestId: Int, request: Job) {
        requests[clientId to requestId] = request
    }

    /**
     * Checks if a request with the given [requestId] is running for the client with [clientId].
     * @return true if a matching request is found, false otherwise
     */
    fun requestRunning(clientId: Int, requestId: Int): Boolean =
        requests.contains(clientId to requestId)


    /**
     * Removes the request with the given [requestId] from the respective client.
     */
    fun removeRequest(clientId: Int, requestId: Int): Boolean =
        requests.remove(clientId to requestId) != null

    /**
     * Cancels the request with the given [requestId] from the respective client.
     */
    fun cancelRequest(clientId: Int, requestId: Int) {
        requests[clientId to requestId]?.cancel("disposed by client")
    }

    /**
     * Cancels all running requests.
     */
    fun cancelRequests() {
        val canceled = mutableListOf<Job>()

        requests.keys.forEach { pair ->
            requests.remove(pair)?.let { job ->
                log.info(this::cancelRequests.name) {
                    "[${pair.first}){${pair.second}] cancel request"
                }

                job.cancel()
                canceled.add(job)

            } ?: run {
                log.warn(this::cancelRequests.name) {
                    "cancel request [${pair.first},${pair.second}] request not found:" +
                            " completed or canceled by client"
                }
            }
        }

        runBlocking {
            canceled.forEach {
                it.join()
            }
        }
    }
}

