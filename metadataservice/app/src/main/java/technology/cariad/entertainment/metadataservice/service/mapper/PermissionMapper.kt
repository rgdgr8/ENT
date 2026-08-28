package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.common.permission.Permission
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataserviceclient.api.PermissionData

class PermissionMapper : Mapper<Permission, Int>() {
    override fun mapValue(value: Permission): Int =
        when (value) {
            Permission.ACCESS_FINE_LOCATION ->
                PermissionData.PERMISSION_FINE_BACKGROUND_LOCATION
            Permission.ACCESS_BACKGROUND_LOCATION ->
                PermissionData.PERMISSION_FINE_BACKGROUND_LOCATION
        }
}

