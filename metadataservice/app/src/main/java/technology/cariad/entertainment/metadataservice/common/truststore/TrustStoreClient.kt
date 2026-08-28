package technology.cariad.entertainment.metadataservice.common.truststore

import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.common.log.logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrustStoreClient @Inject constructor(
    private val trustStoreApiClient: TrustStoreApiClient
) {
    private val log by logger()

    private var preparedWorkspace = false
    private val trustStoreStorage = TrustStoreStorage()
    private var storageRoot = ""

    /**
     * Prepares the Storage root for future truststore storage
     * @param storageRoot The storageRoot direction
     */
    fun prepareStorageRoot(storageRoot: String) {
        log.info(this::prepareStorageRoot.name) {
            "New storage root is $storageRoot"
        }
        this.storageRoot = storageRoot
    }
    /**
     * Subscribes to the TrustStore from the magic adapter
     * @param fake false --> use hardcoded truststore for debugging,
     *             true  --> connect to magic adapter
     */
    fun subscribeTrustStore(fake: Boolean) {
        if (storageRoot == "") {
            log.info(this::subscribeTrustStore.name) {
                "No storage root receive"
            }
            return
        }
        if (!preparedWorkspace) {
            trustStoreApiClient.setTrustStoreStorage(trustStoreStorage)
            trustStoreStorage.prepareWorkspace(storageRoot)
            preparedWorkspace = true

        }
        if (fake || !BuildConfig.GN_LICENSE_ENABLED) {
            log.info(this::subscribeTrustStore.name) {
                "Using default truststore"
            }
        }
        else {
            log.info(this::subscribeTrustStore.name) {
                "Getting truststore by magic"
            }
            trustStoreApiClient.fetchTrustStore()
        }
    }
}
