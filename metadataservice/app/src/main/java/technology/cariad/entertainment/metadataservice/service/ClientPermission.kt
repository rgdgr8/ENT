package technology.cariad.entertainment.metadataservice.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Process
import technology.cariad.entertainment.metadataservice.BuildConfig

/**
 * "USE_SERVICE" permission contract for clients of the MetadataService.
 *
 * The permission name is selected at build time via mds.identity.
 */
object ClientPermission {

    /**
     * Accept both current and legacy permission contracts while clients migrate.
     * Includes a short-lived compatibility alias used by older mds_probe builds.
     */
    private val acceptedUseServicePermissions: Set<String> = setOf(
        BuildConfig.USE_SERVICE_PERMISSION,
        BuildConfig.LEGACY_USE_SERVICE_PERMISSION,
    )

    /**
     * Returns `true` if the current Binder caller is allowed to use the service. Must be
     * called from within a Binder transaction so that the calling identity refers to the
     * client process.
     *
     * Same-app callers (e.g. the app's own `:demo` process, which shares the UID) are
     * always allowed. This mirrors the platform behaviour: when the service was guarded by
     * a manifest `android:permission`, AMS exempted same-UID callers from the check.
     */
    fun hasUseServicePermission(context: Context): Boolean =
        Process.myUid() == Binder.getCallingUid() ||
            acceptedUseServicePermissions.any {
                context.checkCallingPermission(it) == PackageManager.PERMISSION_GRANTED
            }
}
