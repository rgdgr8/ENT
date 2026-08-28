package technology.cariad.entertainment.metadataservice.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.HandlerThread
import android.os.ParcelFileDescriptor
import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.AccessManager
import technology.cariad.entertainment.metadataservice.service.ClientPermission
import java.io.FileNotFoundException
import java.nio.file.Paths

// Must remain aligned with manifest authorities. BuildConfig contains the primary authority used
// for newly generated URIs; legacy authority is accepted for client backward compatibility.
private val URI_AUTHORITY = BuildConfig.IMAGE_PROVIDER_AUTHORITY
private val LEGACY_URI_AUTHORITY = BuildConfig.LEGACY_IMAGE_PROVIDER_AUTHORITY
private val ACCEPTED_URI_AUTHORITIES = setOf(URI_AUTHORITY, LEGACY_URI_AUTHORITY)

open class MetadataImageProvider : ContentProvider() {

    companion object {
        internal val uriPathMapper by lazy {
            UriPathMapper(
                MetadataServiceApp.appComponent.getFileStorageRoot(),
                PathConfigParser.getPathConfig(MetadataServiceApp.appComponent.getContext())
            )
        }

        /**
         * Converts the given path to the respective uri.
         */
        fun getUriForFile(path: String): Uri {
            return uriPathMapper.filePathToUri(URI_AUTHORITY, Paths.get(path))
        }
    }

    private val log by logger()

    private val closeHandlerThread = HandlerThread("ImageUriFdClose")
    private lateinit var closeHandler: Handler

    /**
     * @see ContentProvider.onCreate
     */
    override fun onCreate(): Boolean {

        closeHandlerThread.start()
        closeHandler = Handler(closeHandlerThread.looper)

        return true
    }

    /**
     * @see ContentProvider.openFile
     */
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {

        val callerUid = Binder.getCallingUid()
        val packageName = context?.packageManager?.getPackagesForUid(callerUid)
        if (!MetadataServiceApp.isInitialized) {
            log.warn("openFile") {
                "ignoring request from uid=$callerUid for uri=$uri: provider not yet initialized"
            }
            return null
        }

        log.info("openFile") { "called from uid=$callerUid for uri=$uri with mode=$mode" }

        // The provider is exported; enforce USE_SERVICE permission at request time.
        val ctx = context
        if (ctx == null || !ClientPermission.hasUseServicePermission(ctx)) {
            log.warn("openFile") {
                "denied uid=$callerUid for uri=$uri: missing USE_SERVICE permission"
            }
            IRCLogger.appClientError(
                "Missing USE_SERVICE permission to access $uri from package: $packageName"
            )
            throw SecurityException("caller does not hold USE_SERVICE permission")
        }

        if (!ACCEPTED_URI_AUTHORITIES.contains(uri.authority)) {
            log.error("openFile") {
            "authority doesn't match this content provider's authorities: " +
                "expected=$ACCEPTED_URI_AUTHORITIES actual=${uri.authority}"
            }
            IRCLogger.appClientError("Invalid authority to open $uri from package: $packageName")
            throw SecurityException("invalid authority: ${uri.authority}")
        }

        if (!AccessManager.checkUriPermission(callerUid, uri)) {
            log.warn("openFile") { "caller was not granted permission to access uri=$uri" }
            IRCLogger.appClientError(
                "Invalid authority to access $uri from package: $packageName"
            )
            throw SecurityException("no permission to access resource: $uri")
        }

        if (mode != "r") {
            log.warn("openFile") { "write access not permitted for uri=$uri" }
            IRCLogger.appClientError(
                "Invalid authority for write access to $uri from package: $packageName"
            )
            throw SecurityException("write access requested for read-only resource: $uri")
        }

        val filePath = uriPathMapper.uriToFilePath(uri)

        if (filePath == null) {
            log.warn("openFile") { "failed to parse uri=$uri" }
            throw FileNotFoundException("no file found matching resource: $uri")
        }

        val file = filePath.toFile()

        log.info("openFile") {
            "file uri=$uri path=${file.path} size=${file.length()}B"
        }

        return ParcelFileDescriptor.open(
            file,
            ParcelFileDescriptor.MODE_READ_ONLY,
            closeHandler
        ) {
            if (it == null) {
                log.trace("onClose") {
                    "file descriptor closed by client uid=$callerUid for uri=$uri"
                }
            } else if (it is ParcelFileDescriptor.FileDescriptorDetachedException) {
                log.warn("onClose") {
                    "file descriptor detached by client uid=$callerUid for uri=$uri"
                }
            }
        }
    }

    /**
     * Not required.
     * @see ContentProvider.getType
     */
    override fun getType(uri: Uri): String? {
        return null
    }

    /**
     * Not supported.
     * @see ContentProvider.query
     */
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        throw UnsupportedOperationException("Operation not supported")
    }

    /**
     * Not supported.
     * @see ContentProvider.insert
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        throw UnsupportedOperationException("Operation not supported")
    }

    /**
     * Not supported.
     * @see ContentProvider.update
     */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("Operation not supported")
    }

    /**
     * Not supported.
     * @see ContentProvider.delete
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("Operation not supported")
    }
}
