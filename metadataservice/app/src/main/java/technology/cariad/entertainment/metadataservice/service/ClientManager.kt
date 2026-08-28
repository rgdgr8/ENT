package technology.cariad.entertainment.metadataservice.service

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class ClientManager {
    private val clientCounter: AtomicInteger = AtomicInteger(0)
    private val clientMap: MutableMap<Pair<Int, Int>, String> = ConcurrentHashMap()

    internal fun register(name: String, userId: Int): Int {
        val clientId = clientCounter.getAndIncrement()
        clientMap[clientId to userId] = name
        return clientId
    }

    internal fun unregister(id: Int, userId: Int): Boolean =
        clientMap.remove(id to userId) != null

    internal fun isRegistered(id: Int, userId: Int): Boolean = clientMap.containsKey(id to userId)
}

