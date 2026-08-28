package technology.cariad.entertainment.metadataservice.common.truststore

import android.content.Context
import de.esolutions.magicadapter.generic.TrustStoreCallback
import de.esolutions.magicadapter.stapi.ServiceConnectionCallback
import de.esolutions.magicadapter.stapi.ServiceConnectionFailure
import de.esolutions.magicadapter.stapi.TrustStoreApiBinding
import technology.cariad.entertainment.metadataservice.common.log.logger
import javax.inject.Inject
import javax.net.ssl.TrustManager

class TrustStoreApiClient @Inject constructor (
    context: Context
) {
    private val log by logger()

    private val bindingTruststore = TrustStoreApiBinding(context)
    private val trustStoreId = "gracenote"

    private lateinit var trustStoreStorage : TrustStoreStorage

    internal fun fetchTrustStore(){
        try {
            val trustStoreCallback = object : TrustStoreCallback {
                override fun trustStoreByNameResponse(trustStoreId: String, trustStoreAsPemString: String?) {
                    log.info(this::trustStoreByNameResponse.name) {
                        "trustStoreByNameResponse: got $trustStoreAsPemString for $trustStoreId"
                    }
                    trustStoreStorage.updateTrustStore(trustStoreAsPemString)
                }

                override fun trustStoreManagerResponse(operationId: String, trustManager: TrustManager?) {
                    log.info(this::trustStoreByNameResponse.name) {
                        "trustStoreManagerResponse: got $trustManager for $operationId"
                    }
                }
            }

            val serviceConnectionCallback = object : ServiceConnectionCallback {
                override fun serviceConnected() {
                    log.info(this::serviceConnected.name) {
                        "serviceConnected: requesting truststores"
                    }
                    bindingTruststore.fetchTrustStore(trustStoreId, trustStoreCallback)
                    bindingTruststore.fetchTrustManager("myOperationId", trustStoreCallback)
                }
                override fun serviceDisconnected(p0: ServiceConnectionFailure?) {
                    log.info(this::serviceDisconnected.name) {
                        "serviceDisconnected: trustStoreApiBinding sent serviceDisconnected"
                    }
                }
            }

            bindingTruststore.connect(serviceConnectionCallback, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            log.error(this::fetchTrustStore.name) {
                "onCreate: error when initializing magicAdapter ${e.message}"
            }
        }
    }

    /**
    * Set the TrustStoreStorage for this class
    * @param trustStoreStorage The new TrustStoreStorage
     */
    fun setTrustStoreStorage(trustStoreStorage : TrustStoreStorage) {
        this.trustStoreStorage = trustStoreStorage
    }
}
