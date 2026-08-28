package technology.cariad.entertainment.metadataservice.common.servicelist

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ServiceListApiClientAndroidTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var client: ServiceListApiClient

    @Before
    fun setUp() {
        client = ServiceListApiClient(context)
    }

    @Test
    fun asFlow_shouldConnectToServiceAndEmitServiceListEntries() {
        runBlocking {
            val list = client.asFlow().take(2).toList()
            Assert.assertTrue(list[0].isEmpty())
            Assert.assertEquals(1, list[1].size)
        }
    }
}

