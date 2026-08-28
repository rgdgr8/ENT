package technology.cariad.entertainment.metadataservice.common.disablereasons

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class DisableReasonsApiClientAndroidTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var client: DisableReasonsApiClient

    @Before
    fun setUp() {
        client = DisableReasonsApiClient(context)
    }

    @Test
    fun asFlow_shouldConnectToServiceAndEmitDisableReasons() {
        runBlocking {
            val list = client.asFlow().take(2).toList()
            Assert.assertTrue(list[0].isEmpty())
            Assert.assertEquals(1, list[1].size)
        }
    }

    @Test
    @Ignore("test is non deterministic and may sporadically fail")
    fun hasAnyDisableReasons_shouldReturnFalseWhileServiceConnected() {
        runBlocking {
            client.asFlow().take(2).collect() {
                if (it.size == 1) {
                    val disabled = client.hasAnyDisableReasons(ServiceIdentifier.GRACENOTE_V1)
                    Assert.assertFalse(disabled)
                }
            }
        }
    }

    @Test
    fun hasAnyDisableReasons_shouldReturnTrueWhileServiceDisconnected() {
        val disabled = client.hasAnyDisableReasons(ServiceIdentifier.GRACENOTE_V1)
        Assert.assertTrue(disabled)
    }
}

