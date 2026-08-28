package technology.cariad.entertainment.metadataservice.common.permission

interface IPermissionManager {

    /**
     * Checks whether permission has already been granted or not.
     * @param permission the permission
     * @return true if has permission otherwise false
     */
    fun hasPermission(permission: Permission): Boolean

    /**
     * Returns list of denied permissions.
     */
    fun getDeniedPermissions(): List<Permission>
}

