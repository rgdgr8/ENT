package technology.cariad.entertainment.metadataservice.common.database.android

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import technology.cariad.entertainment.metadataservice.common.database.android.IDatabaseEventListener.EventProcessing
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifySequence
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test

class DatabaseCallbackInterceptorTest {

    private val defaultCallbackMock = mockk<SupportSQLiteOpenHelper.Callback>(relaxUnitFun = true)
    private val eventListenerMock = mockk<IDatabaseEventListener>(relaxUnitFun = true)
    private val databaseMock = mockk<SupportSQLiteDatabase>()

    private val databaseCallbackInterceptor = DatabaseCallbackInterceptor.create(
        eventListenerMock,
        defaultCallbackMock
    )

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun onConfigure_shouldRunDefaultConfigureIfBeforeReturnsContinue() {
        every { eventListenerMock.beforeConfigure(any()) } returns EventProcessing.CONTINUE

        databaseCallbackInterceptor.onConfigure(databaseMock)

        verifySequence {
            eventListenerMock.beforeConfigure(databaseMock)
            defaultCallbackMock.onConfigure(databaseMock)
            eventListenerMock.afterConfigure(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onConfigure_shouldSkipDefaultConfigureIfBeforeReturnsHandled() {
        every { eventListenerMock.beforeConfigure(any()) } returns EventProcessing.HANDLED

        databaseCallbackInterceptor.onConfigure(databaseMock)

        verify {
            eventListenerMock.beforeConfigure(databaseMock)
            defaultCallbackMock wasNot Called
        }

        verify(inverse = true) {
            eventListenerMock.afterConfigure(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onCreate_shouldRunDefaultConfigureIfBeforeReturnsContinue() {
        every { eventListenerMock.beforeCreate(any()) } returns EventProcessing.CONTINUE

        databaseCallbackInterceptor.onCreate(databaseMock)

        verifySequence {
            eventListenerMock.beforeCreate(databaseMock)
            defaultCallbackMock.onCreate(databaseMock)
            eventListenerMock.afterCreate(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onCreate_shouldSkipDefaultConfigureIfBeforeReturnsHandled() {
        every { eventListenerMock.beforeCreate(any()) } returns EventProcessing.HANDLED

        databaseCallbackInterceptor.onCreate(databaseMock)

        verify {
            eventListenerMock.beforeCreate(databaseMock)
            defaultCallbackMock wasNot Called
        }

        verify(inverse = true) {
            eventListenerMock.afterCreate(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onUpgrade_shouldRunDefaultConfigureIfBeforeReturnsContinue() {
        val oldVersion = 1
        val newVersion = 2

        every {
            eventListenerMock.beforeUpgrade(any(), any(), any())
        } returns EventProcessing.CONTINUE

        databaseCallbackInterceptor.onUpgrade(databaseMock, oldVersion, newVersion)

        verifySequence {
            eventListenerMock.beforeUpgrade(databaseMock, oldVersion, newVersion)
            defaultCallbackMock.onUpgrade(databaseMock, oldVersion, newVersion)
            eventListenerMock.afterUpgrade(databaseMock, oldVersion, newVersion)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onUpgrade_shouldSkipDefaultConfigureIfBeforeReturnsHandled() {
        val oldVersion = 1
        val newVersion = 2

        every {
            eventListenerMock.beforeUpgrade(any(), any(), any())
        } returns EventProcessing.HANDLED

        databaseCallbackInterceptor.onUpgrade(databaseMock, oldVersion, newVersion)

        verify {
            eventListenerMock.beforeUpgrade(databaseMock, oldVersion, newVersion)
            defaultCallbackMock wasNot Called
        }

        verify(inverse = true) {
            eventListenerMock.afterUpgrade(databaseMock, oldVersion, newVersion)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onDowngrade_shouldRunDefaultConfigureIfBeforeReturnsContinue() {
        val oldVersion = 2
        val newVersion = 1

        every {
            eventListenerMock.beforeDowngrade(any(), any(), any())
        } returns EventProcessing.CONTINUE

        databaseCallbackInterceptor.onDowngrade(databaseMock, oldVersion, newVersion)

        verifySequence {
            eventListenerMock.beforeDowngrade(databaseMock, oldVersion, newVersion)
            defaultCallbackMock.onDowngrade(databaseMock, oldVersion, newVersion)
            eventListenerMock.afterDowngrade(databaseMock, oldVersion, newVersion)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onDowngrade_shouldSkipDefaultConfigureIfBeforeReturnsHandled() {
        val oldVersion = 1
        val newVersion = 2

        every {
            eventListenerMock.beforeDowngrade(any(), any(), any())
        } returns EventProcessing.HANDLED

        databaseCallbackInterceptor.onDowngrade(databaseMock, oldVersion, newVersion)

        verify {
            eventListenerMock.beforeDowngrade(databaseMock, oldVersion, newVersion)
            defaultCallbackMock wasNot Called
        }

        verify(inverse = true) {
            eventListenerMock.afterDowngrade(databaseMock, oldVersion, newVersion)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onOpen_shouldRunDefaultConfigureIfBeforeReturnsContinue() {
        every { eventListenerMock.beforeOpen(any()) } returns EventProcessing.CONTINUE

        databaseCallbackInterceptor.onOpen(databaseMock)

        verifySequence {
            eventListenerMock.beforeOpen(databaseMock)
            defaultCallbackMock.onOpen(databaseMock)
            eventListenerMock.afterOpen(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onOpen_shouldSkipDefaultConfigureIfBeforeReturnsHandled() {
        every { eventListenerMock.beforeOpen(any()) } returns EventProcessing.HANDLED

        databaseCallbackInterceptor.onOpen(databaseMock)

        verify {
            eventListenerMock.beforeOpen(databaseMock)
            defaultCallbackMock wasNot Called
        }

        verify(inverse = true) {
            eventListenerMock.afterOpen(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onCorruption_shouldRunDefaultConfigureIfBeforeReturnsContinue() {
        every { eventListenerMock.beforeCorruption(any()) } returns EventProcessing.CONTINUE

        databaseCallbackInterceptor.onCorruption(databaseMock)

        verifySequence {
            eventListenerMock.beforeCorruption(databaseMock)
            defaultCallbackMock.onCorruption(databaseMock)
            eventListenerMock.afterCorruption(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @Test
    fun onCorruption_shouldSkipDefaultConfigureIfBeforeReturnsHandled() {
        every { eventListenerMock.beforeCorruption(any()) } returns EventProcessing.HANDLED

        databaseCallbackInterceptor.onCorruption(databaseMock)

        verify {
            eventListenerMock.beforeCorruption(databaseMock)
            defaultCallbackMock wasNot Called
        }

        verify(inverse = true) {
            eventListenerMock.afterCorruption(databaseMock)
        }

        confirmVerified(eventListenerMock, defaultCallbackMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

