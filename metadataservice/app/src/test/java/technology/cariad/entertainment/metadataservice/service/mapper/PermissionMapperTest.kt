package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.common.permission.Permission
import technology.cariad.entertainment.metadataserviceclient.api.PermissionData
import org.junit.Assert
import org.junit.Test

class PermissionMapperTest {

    @Test
    fun mapValue_shouldConvertPermissionEnumToFlag() {
        val permissionFlag = PermissionMapper().mapValue(Permission.ACCESS_FINE_LOCATION)

        Assert.assertEquals(
            PermissionData.PERMISSION_FINE_BACKGROUND_LOCATION,
            permissionFlag
        )
    }

    @Test
    fun mapList_shouldConvertPermissionEnumsToFlags() {
        val permissions = listOf(
            Permission.ACCESS_BACKGROUND_LOCATION,
            Permission.ACCESS_FINE_LOCATION,
        )

        val permissionFlags = PermissionMapper().mapList(permissions)

        val expectedPermissionFlags = listOf(
            PermissionData.PERMISSION_FINE_BACKGROUND_LOCATION,
            PermissionData.PERMISSION_FINE_BACKGROUND_LOCATION
        )

        Assert.assertEquals(expectedPermissionFlags, permissionFlags)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val emptyList = PermissionMapper().mapList(listOf())

        Assert.assertEquals(listOf<Int>(), emptyList)
    }
}

