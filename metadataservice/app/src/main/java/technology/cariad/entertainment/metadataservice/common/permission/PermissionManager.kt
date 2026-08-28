package technology.cariad.entertainment.metadataservice.common.permission

import android.content.Context
import android.content.pm.PackageManager
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger

class PermissionManager(private val context: Context) : IPermissionManager {

    private val log by logger()

    /**
     * @see [IPermissionManager.hasPermission]
     */
    override fun hasPermission(permission: Permission): Boolean {
        return (context.checkSelfPermission(permission.string) == PackageManager.PERMISSION_GRANTED)
            .also {
                log.info(this::hasPermission.name) {
                    "permission=${permission.name} granted=$it"
                }
                if (!it) {
                    IRCLogger.appPermissionError(permission.name)
                }
            }
    }

    /**
     * @see [IPermissionManager.getDeniedPermissions]
     */
    override fun getDeniedPermissions(): List<Permission> {
        return Permission.values()
            .filter { context.checkSelfPermission(it.string) == PackageManager.PERMISSION_DENIED }
            .also { list ->
                val deniedPermissions = list.joinToString { it.string }
                log.info(this::getDeniedPermissions.name) {
                    "denied permissions=[$deniedPermissions]"
                }
                IRCLogger.appPermissionError(deniedPermissions)
            }
    }
}

