package technology.cariad.entertainment.metadataservice.common.permission

import android.content.Context
import android.content.pm.PackageManager
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PermissionManagerTest {

    private val contextMock = mockk<Context>()
    private val permissionManager = PermissionManager(contextMock)

    @Before
    fun setup() {
        LoggerMock()
    }

    @Test
    fun hasPermission_shouldReturnFalseIfDenied() {
        every { contextMock.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_DENIED

        val granted = permissionManager.hasPermission(Permission.ACCESS_BACKGROUND_LOCATION)

        Assert.assertEquals(false, granted)
    }

    @Test
    fun hasPermission_shouldReturnTrueIfGranted() {
        every { contextMock.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED

        val granted = permissionManager.hasPermission(Permission.ACCESS_BACKGROUND_LOCATION)

        Assert.assertEquals(true, granted)
    }

    @Test
    fun getDeniedPermissions_shouldReturnNotGrantedPermissions() {
        every { contextMock.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) } returns PackageManager.PERMISSION_GRANTED
        every { contextMock.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) } returns PackageManager.PERMISSION_DENIED

        val denied = permissionManager.getDeniedPermissions()

        Assert.assertEquals(listOf(Permission.ACCESS_FINE_LOCATION), denied)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

