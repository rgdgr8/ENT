package technology.cariad.entertainment.metadataservice.common.position

import com.volkswagenag.nav.positioninformation.GeneralPositionInformationType
import com.volkswagenag.nav.positioninformation.IPositionInformation
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.serviceconnection.NullBindingException
import technology.cariad.entertainment.metadataservice.common.serviceconnection.ServiceConnection
import technology.cariad.entertainment.metadataservice.common.serviceconnection.ServiceUnavailableException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest

class PositionProviderApi(
    private val serviceConnection: ServiceConnection,
    private val updateIntervalMs: Long
) {
    private val log by logger()

    internal fun positionFlow(): Flow<GeneralPositionInformationType?> {
        return serviceConnection.serviceConnectionFlow()
            .catch {

                log.warn("positionFlow_catch") {
                    "exception caught from service connection: message=${it.message}"
                }

                when (it) {
                    is ServiceUnavailableException -> throw(UnsupportedOperationException())
                    is NullBindingException -> throw(UnsupportedOperationException())
                }
            }.flatMapLatest { binder ->
                callbackFlow {
                    val service = IPositionInformation.Stub.asInterface(binder)
                    val callback = PositionInformationCallback.create(this)

                    log.info("positionFlow_flatMapLatest") {
                        "register position information callback"
                    }

                    service?.registerPositionInformation(updateIntervalMs, callback)

                    awaitClose {
                        log.info("positionFlow_flatMapLatest") {
                            "unregister position information callback"
                        }

                        service?.unRegisterPositionInformation(callback)
                    }
                }
            }
    }
}

