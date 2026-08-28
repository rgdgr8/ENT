// LOCAL BUILD STUB: IGracenoteMusicDataCallback for local build without gmdlib dependency
package de.esolutions.gmdlib.media

import de.esolutions.gmdlib.data.GmdErrorType

/**
 * Stub interface for IGracenoteMusicDataCallback.
 * This is a placeholder for local builds without the gmdlib dependency.
 */
interface IGracenoteMusicDataCallback {
    fun onSuccess(response: String)
    fun onError(errorType: GmdErrorType, httpStatusCode: Int?, message: String)
}
