package technology.cariad.entertainment.metadataservice.common.serviceconnection

import android.content.Context
import android.content.Intent
import android.os.IBinder
import technology.cariad.entertainment.metadataservice.common.serviceresolver.ServiceResolver
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ServiceConnectionTest {

    private val contextMock = mockk<Context>(relaxed = true)
    private val serviceConnectionCallbackMock = mockk<ServiceConnectionCallback>(relaxed = true)
    private val serviceResolverMock = mockk<ServiceResolver>()
    private val intentMock = mockk<Intent>(relaxed = true)

    private val serviceConnection =
        ServiceConnection(serviceResolverMock, contextMock, "de.test.servicce")

    private val channelSlot = slot<ProducerScope<IBinder?>>()

    @Before
    fun setup() {
        mockkObject(ServiceConnectionCallback)
        every { ServiceConnectionCallback.create(capture(channelSlot)) } returns serviceConnectionCallbackMock

        every { serviceResolverMock.resolve(any()) } returns intentMock
    }

    @Test
    fun test() = runBlockingTest {

        every { contextMock.bindService(any(), any(), any<Int>()) } returns true

        val updates = mutableListOf<IBinder?>()
        val job = launch { serviceConnection.serviceConnectionFlow().toList(updates) }

        channelSlot.captured.trySend(null)
        channelSlot.captured.close(BindingDiedException())
        channelSlot.captured.trySend(null)

        Assert.assertEquals(2, updates.size)

        job.cancel()
    }

    @Test
    fun serviceConnectionFlow_shouldRetryOnBindingDied() = runBlockingTest {

        every { contextMock.bindService(any(), any(), any<Int>()) } returns true

        val job = launch { serviceConnection.serviceConnectionFlow().collect { } }

        channelSlot.captured.close(BindingDiedException())

        verifySequence {
            contextMock.bindService(any(), any(), any<Int>())
            contextMock.unbindService(any())
            contextMock.bindService(any(), any(), any<Int>())
        }

        confirmVerified(contextMock)

        job.cancel()
    }

    @Test(expected = NullBindingException::class)
    fun serviceConnectionFlow_shouldThrowOnNullBinding() = runBlockingTest {
        every { contextMock.bindService(any(), any(), any<Int>()) } returns true

        val job = launch { serviceConnection.serviceConnectionFlow().collect { } }
        channelSlot.captured.close(NullBindingException())
        job.join()
    }

    @Test
    fun serviceConnectionFlow_shouldUnbindOnClose() = runBlockingTest {
        every { contextMock.bindService(any(), any(), any<Int>()) } returns true

        val job = serviceConnection.serviceConnectionFlow().launchIn(this)

        channelSlot.captured.close()

        job.join()

        verify {
            contextMock.unbindService(any())
        }
    }

    @Test
    fun serviceConnectionFlow_shouldNotUnbindWhenBindingFailed() = runBlockingTest {
        every { contextMock.bindService(any(), any(), any<Int>()) } returns false

        val job = serviceConnection.serviceConnectionFlow().catch {}.launchIn(this)

        job.join()

        verify(inverse = true) {
            contextMock.unbindService(any())
        }

        verify {
            contextMock.bindService(any(), any(), any<Int>())
        }

        confirmVerified(contextMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

