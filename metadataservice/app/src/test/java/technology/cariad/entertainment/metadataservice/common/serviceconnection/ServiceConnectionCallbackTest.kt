package technology.cariad.entertainment.metadataservice.common.serviceconnection

import android.content.ComponentName
import android.os.IBinder
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ServiceConnectionCallbackTest {

    private val componentName = mockk<ComponentName>(relaxed = true)
    private val binderMock = mockk<IBinder>()

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun onServiceConnected_shouldEmitBinder() = testScope.runBlockingTest {
        val flow = channelFlow<IBinder?> {
            val serviceConnectionCallback = ServiceConnectionCallback.create(this)
            serviceConnectionCallback.onServiceConnected(componentName, binderMock)
        }

        val binders = flow.toList()

        Assert.assertEquals(listOf(binderMock), binders)
    }

    @Test
    fun onServiceDisconnected_shouldEmitNull() = testScope.runBlockingTest {
        val flow = channelFlow<IBinder?> {
            val serviceConnectionCallback = ServiceConnectionCallback.create(this)
            serviceConnectionCallback.onServiceDisconnected(componentName)
        }

        val binders = flow.toList()

        Assert.assertEquals(listOf<IBinder?>(null), binders)
    }

    @Test(expected = BindingDiedException::class)
    fun onBindingDied_shouldEmitBindingDiedException() = testScope.runBlockingTest {
        val flow = channelFlow<IBinder?> {
            val serviceConnectionCallback = ServiceConnectionCallback.create(this)
            serviceConnectionCallback.onBindingDied(componentName)
        }

        flow.toList()
    }

    @Test(expected = NullBindingException::class)
    fun onNullBinding_shouldEmitNullBindingException() = testScope.runBlockingTest {
        val flow = channelFlow<IBinder?> {
            val serviceConnectionCallback = ServiceConnectionCallback.create(this)
            serviceConnectionCallback.onNullBinding(componentName)
        }

        flow.toList()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

