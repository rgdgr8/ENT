package technology.cariad.entertainment.metadataservice.common.codingprovider

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.coroutines.suspendCoroutine

@RunWith(AndroidJUnit4::class)
class MediaCodingProviderAndroidTest {

    private val codingProvider =
        MediaCodingProvider(ApplicationProvider.getApplicationContext())

    @Before
    fun setup() = runBlocking {
        suspendCoroutine { cont ->
            val listener = MediaCodingProviderListenerTest(cont)
            codingProvider.start(listener)
        }
    }

    @Test
    fun getCountryOfSalesShouldNonNullValue() {
        assert(codingProvider.getCountryOfSales() != null)
    }

    @After
    fun tearDown() {
        codingProvider.stop()
    }
}

