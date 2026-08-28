package technology.cariad.entertainment.metadataservice.service

import android.net.Uri
import technology.cariad.entertainment.metadataservice.common.log.logger
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object AccessManager {

    private val log by logger()

    private val folderGrants = mutableMapOf<Int, MutableList<String>>()
    private val folderGrantsLock = ReentrantReadWriteLock(true)

    /**
     * Grants the client running under the given [uid] read access to [uri].
     * @param uid User id of the client.
     * @param uri Resource the client wants to access.
     */
    @Synchronized
    fun grantUriPermission(uid: Int, uri: String) {
        val decodedUri = Uri.parse(uri)

        if (decodedUri == Uri.EMPTY || decodedUri.path.isNullOrEmpty()) {
            log.error(this::grantUriPermission.name) { "called with invalid uri=$uri (uid=$uid)" }
            return
        }

        val folder = decodedUri.path!!.removeSuffix(decodedUri.lastPathSegment ?: "")

        folderGrantsLock.write {
            if (folderGrants[uid] == null) {
                folderGrants[uid] = mutableListOf()
            }

            folderGrants[uid]?.apply {
                add(folder)
            }
        }

        log.info(this::grantUriPermission.name) { "access granted for uid=$uid to uri=$decodedUri" }
    }

    /**
     * Checks if the client running under the given [uid] is allowed to access [uri].
     * @param uid User id of the client.
     * @param uri Resource the client wants to access
     */
    fun checkUriPermission(uid: Int, uri: Uri): Boolean {

        if (uri == Uri.EMPTY || uri.path == null) {
            log.warn(this::checkUriPermission.name) { "called with invalid uri=$uri (uid=$uid)" }
            return false
        }

        val folder = uri.path!!.removeSuffix(uri.lastPathSegment ?: "")

        val accessGranted = folderGrantsLock.read {
            folderGrants[uid]?.contains(folder) ?: false
        }

        log.info(this::checkUriPermission.name) {
            "access for uid=$uid to uri=$uri allowed=$accessGranted"
        }

        return accessGranted
    }
}

