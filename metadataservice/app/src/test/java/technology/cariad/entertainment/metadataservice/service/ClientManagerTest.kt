package technology.cariad.entertainment.metadataservice.service

import org.junit.Assert
import org.junit.Test

private const val THIS_CLIENT_NAME = "this client"
private const val THIS_CLIENT_UID = 1

private const val OTHER_CLIENT_NAME = "other client"
private const val OTHER_CLIENT_UID = 2

class ClientManagerTest {

    private val clientManager = ClientManager()

    @Test
    fun register_returnsNewClientId() {
        val firstId = clientManager.register(THIS_CLIENT_NAME, THIS_CLIENT_UID)
        val secondId = clientManager.register(OTHER_CLIENT_NAME, OTHER_CLIENT_UID)

        Assert.assertNotEquals(firstId, secondId)
    }

    @Test
    fun register_allowsMultipleRegistrationsFromSameClient() {
        val firstId = clientManager.register(THIS_CLIENT_NAME, THIS_CLIENT_UID)
        val secondId = clientManager.register(THIS_CLIENT_NAME, THIS_CLIENT_UID)

        Assert.assertNotEquals(firstId, secondId)
    }

    @Test
    fun isRegistered_returnsTrueIfThisClientIsRegistered() {
        val id = clientManager.register(THIS_CLIENT_NAME, THIS_CLIENT_UID)

        val registered = clientManager.isRegistered(id, THIS_CLIENT_UID)

        Assert.assertTrue(registered)
    }

    @Test
    fun isRegistered_returnsFalseIfNoClientIsRegistered() {
        val registered = clientManager.isRegistered(0, THIS_CLIENT_UID)

        Assert.assertFalse(registered)
    }

    @Test
    fun isRegistered_returnsFalseIfThisClientIsNotRegistered() {
        clientManager.register(OTHER_CLIENT_NAME, OTHER_CLIENT_UID)

        val registered = clientManager.isRegistered(0, THIS_CLIENT_UID)

        Assert.assertFalse(registered)
    }

    @Test
    fun unregister_removesClientRegistration() {
        val id = clientManager.register(THIS_CLIENT_NAME, THIS_CLIENT_UID)
        var registered = clientManager.isRegistered(id, THIS_CLIENT_UID)

        Assert.assertTrue(registered)

        clientManager.unregister(id, THIS_CLIENT_UID)
        registered = clientManager.isRegistered(id, THIS_CLIENT_UID)

        Assert.assertFalse(registered)
    }
}

