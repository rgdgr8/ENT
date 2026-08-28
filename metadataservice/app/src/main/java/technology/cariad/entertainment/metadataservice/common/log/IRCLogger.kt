package technology.cariad.entertainment.metadataservice.common.log

import android.util.Log

/**
 * LOCAL BUILD STUB: IRC Logger for telemetry/diagnostics.
 * 
 * This replaces de.esolutions.fw.gen.tbirc.mmx.MetadataServiceIRC with no-op implementations.
 * All telemetry calls are logged to Android Log for debugging purposes but no IRC data is sent.
 */
object IRCLogger {

    private const val TAG = "mds/IRCLogger"

    const val ESM_NOT_AVAILABLE_ERROR = 0
    const val DB_CORRUPTION_ERROR = 1
    const val DB_MIGRATION_ERROR = 2
    const val NO_NETWORK_AVAILABLE_ERROR = 3
    const val NO_POSITIONING_DATA_AVAILABLE_ERROR = 4
    const val GN_SERVICE_QUEUE_LIMIT_ERROR = 5

    const val CONNECTIVITY_REASON_GROUP = "reasonConnectivity"
    const val CONFIGURATION_REASON_GROUP = "reasonConfiguration"
    const val BACKEND_REASON_GROUP = "reasonBackend"
    const val LICENSE_REASON_GROUP = "reasonLicense"

    val ErrorSet = setOf(
        ESM_NOT_AVAILABLE_ERROR,
        DB_CORRUPTION_ERROR,
        DB_MIGRATION_ERROR,
        NO_NETWORK_AVAILABLE_ERROR,
        NO_POSITIONING_DATA_AVAILABLE_ERROR,
        GN_SERVICE_QUEUE_LIMIT_ERROR
    )

    /**
     * Logs once the current version of MetadataService App after startup
     * LOCAL BUILD: Logs to Android Log instead of IRC
     * @param major
     * @param minor
     * @param patch
     */
    fun appVersionInfo(major: Short, minor: Short, patch: Short) {
        Log.i(TAG, "AppVersionInfo: $major.$minor.$patch (LOCAL BUILD - IRC disabled)")
    }

    /**
     * Logs errors from MetadataService App
     * LOCAL BUILD: Logs to Android Log instead of IRC
     * @param error the status according to predefined errors [ErrorSet]
     */
    fun appError(error: Int) {
        Log.w(TAG, "AppError: $error (LOCAL BUILD - IRC disabled)")
    }

    /**
     * Logs needed permission that is not granted to MetadataService App
     * LOCAL BUILD: Logs to Android Log instead of IRC
     * @param permission
     */
    fun appPermissionError(permission: String) {
        Log.w(TAG, "AppPermissionError: $permission (LOCAL BUILD - IRC disabled)")
    }

    /**
     * Logs requests from not-authorized clients to MetadataService App
     * LOCAL BUILD: Logs to Android Log instead of IRC
     * @param clientPackage client package
     */
    fun appClientError(clientPackage: String) {
        Log.w(TAG, "AppClientError: $clientPackage (LOCAL BUILD - IRC disabled)")
    }

    /**
     * Logs servicelist errors related to MetadataService App
     * LOCAL BUILD: Logs to Android Log instead of IRC
     * @param serviceId
     * @param disableCategory
     * @param disableReason
     */
    fun appServiceListError(serviceId: String, disableCategory: String, disableReason: String) {
        Log.w(TAG, "AppServiceListError: serviceId=$serviceId, category=$disableCategory, reason=$disableReason (LOCAL BUILD - IRC disabled)")
    }

    /**
     * Logs generic errors from MetadataService App
     * LOCAL BUILD: Logs to Android Log instead of IRC
     * @param error
     * @param envData additional data related to the error
     */
    fun appGenericError(error: String, envData: String) {
        Log.w(TAG, "AppGenericError: $error, envData=$envData (LOCAL BUILD - IRC disabled)")
    }
}


