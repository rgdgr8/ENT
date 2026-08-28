package technology.cariad.entertainment.metadataservice.common.codingprovider

import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume


class MediaCodingProviderListenerTest(private val continuation: Continuation<Unit>) :
    IMediaCodingProviderListener {
    private val log by logger()


    override fun onCodingBlobAvailable(blobID: Int, shortArray: ShortArray) {
        log.info(this::onCodingBlobAvailable.name) {
            "[coding_provider] onCodingBlobAvailable"
        }
    }

    override fun onConnectionDisconnected() {
        log.info(this::onConnectionDisconnected.name) {
            "[coding_provider] onConnectionDisconnected"
        }
    }

    override fun onConnectionError() {
        log.info(this::onConnectionError.name) {
            "[coding_provider] onConnectionError"
        }
    }

    override fun onConnectionEstablished() {
        log.info(this::onConnectionEstablished.name) {
            "[coding_provider] onConnectionEstablished"
        }
    }

    override fun onCountryOfSalesAvailable() {
        log.info(this::onCountryOfSalesAvailable.name) {
            "[coding_provider] onCountryOfSalesAvailable"
        }

        continuation.resume(Unit)
    }
}

