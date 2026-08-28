package technology.cariad.entertainment.metadataservice.common.extendedstorage

import android.content.Context
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
//import com.harman.extendedstoragemanager.ExtendedStorageManager as ExtendedStorageManagerExt

private const val EXTERNAL_PARTITION = "metadataservice-data"
private const val ACCESS_MODE_READ_WRITE = 1

class ExtendedStorageManagerTest {

    init {
        LoggerMock()
    }

    private val contextMock = mockk<Context>(relaxed = true)
    //private val extendedStorageManagerMock = mockk<ExtendedStorageManagerExt>()

    private val extendedStorageManager = ExtendedStorageManager(contextMock)

    private val callbackSlot = slot<Runnable>()

    @Before
    fun setup() {
       /* mockkStatic(ExtendedStorageManagerExt::class)

        every {
            ExtendedStorageManagerExt.create(any(), capture(callbackSlot))
        } returns extendedStorageManagerMock*/
    }

    @Test
    fun initialize_shouldCallbackWithFolderOnSuccess() = runBlockingTest {
        /*every {
            extendedStorageManagerMock.getUsageFolder(EXTERNAL_PARTITION, ACCESS_MODE_READ_WRITE)
        } returns hashMapOf(1 to "/path/to/metadata-data")

        suspendCoroutine { cont ->
            extendedStorageManager.initialize {
                Assert.assertNotNull(it)
                cont.resume(Unit)
            }
            callbackSlot.captured.run()
        }*/
    }

    @Test
    fun initialize_shouldCallbackWithNullIfMetadataWriteFolderIsNotAvailable() = runBlockingTest {
        /*every {
            extendedStorageManagerMock.getUsageFolder(EXTERNAL_PARTITION, ACCESS_MODE_READ_WRITE)
        } returns hashMapOf()*/

       /* suspendCoroutine { cont ->
            extendedStorageManager.initialize {
                Assert.assertNull(it)
                cont.resume(Unit)
            }
            callbackSlot.captured.run()
        }*/
    }

    @Test
    fun initialize_shouldCallbackWithNullIfFolderMapIsNotAvailable() = runBlockingTest {
       /* every {
            extendedStorageManagerMock.getUsageFolder(EXTERNAL_PARTITION, ACCESS_MODE_READ_WRITE)
        } returns null*/

      /*  suspendCoroutine { cont ->
            extendedStorageManager.initialize {
                Assert.assertNull(it)
                cont.resume(Unit)
            }
            callbackSlot.captured.run()
        }*/
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

