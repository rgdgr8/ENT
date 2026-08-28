// LOCAL BUILD STUB: GmdClient for local build without gmdlib dependency
package de.esolutions.gmdlib.media

import android.content.Context
import android.net.Network
import de.esolutions.gmdlib.data.AccountManagerConfig
import de.esolutions.gmdlib.data.EndpointConfig
import de.esolutions.gmdlib.data.GmdErrorType

/**
 * Stub class for GmdClient.
 * This is a placeholder for local builds without the gmdlib dependency.
 * All methods are no-op or return stub error responses.
 */
class GmdClient(
    private val context: Context,
    private val processName: String
) {
    private var network: Network? = null
    private var accountManagerConfig: AccountManagerConfig? = null
    private var endpointConfigArtists: EndpointConfig? = null
    private var endpointConfigAlbums: EndpointConfig? = null
    private var endpointConfigRecording: EndpointConfig? = null
    private var endpointConfigRecordingStringSearch: EndpointConfig? = null

    /**
     * Sets the network to use for requests.
     */
    fun setNetwork(network: Network?) {
        this.network = network
    }

    /**
     * Sets the configuration for the client.
     */
    fun setConfig(
        accountManagerConfig: AccountManagerConfig,
        endpointConfigArtists: EndpointConfig,
        endpointConfigAlbums: EndpointConfig,
        endpointConfigRecording: EndpointConfig,
        endpointConfigRecordingStringSearch: EndpointConfig
    ) {
        this.accountManagerConfig = accountManagerConfig
        this.endpointConfigArtists = endpointConfigArtists
        this.endpointConfigAlbums = endpointConfigAlbums
        this.endpointConfigRecording = endpointConfigRecording
        this.endpointConfigRecordingStringSearch = endpointConfigRecordingStringSearch
    }

    /**
     * Gets album metadata by ID.
     * STUB: Always returns error indicating service unavailable.
     */
    fun getAlbumMetadata(albumId: String, callback: IGracenoteMusicDataCallback) {
        // Stub: Return error indicating gmdlib is not available
        callback.onError(
            GmdErrorType.EMPTY_RESPONSE,
            503,
            "GmdClient stub: gmdlib not available in local build"
        )
    }

    /**
     * Gets artist metadata by ID.
     * STUB: Always returns error indicating service unavailable.
     */
    fun getArtistMetadata(artistId: String, callback: IGracenoteMusicDataCallback) {
        // Stub: Return error indicating gmdlib is not available
        callback.onError(
            GmdErrorType.EMPTY_RESPONSE,
            503,
            "GmdClient stub: gmdlib not available in local build"
        )
    }

    /**
     * Gets recording metadata by ID.
     * STUB: Always returns error indicating service unavailable.
     */
    fun getRecordingMetadata(recordingId: String, callback: IGracenoteMusicDataCallback) {
        // Stub: Return error indicating gmdlib is not available
        callback.onError(
            GmdErrorType.EMPTY_RESPONSE,
            503,
            "GmdClient stub: gmdlib not available in local build"
        )
    }

    /**
     * Gets recording metadata by name and artist.
     * STUB: Always returns error indicating service unavailable.
     */
    fun getRecordingMetadata(
        recordingName: String,
        artistName: String,
        callback: IGracenoteMusicDataCallback
    ) {
        // Stub: Return error indicating gmdlib is not available
        callback.onError(
            GmdErrorType.EMPTY_RESPONSE,
            503,
            "GmdClient stub: gmdlib not available in local build"
        )
    }
}
