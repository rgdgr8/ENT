package technology.cariad.entertainment.metadataservice.common.codingprovider.service

import de.esolutions.android.framework.clients.IServiceCallback
import technology.cariad.entertainment.metadataservice.stubs.fw.asi.IStorageServiceListener
import technology.cariad.entertainment.metadataservice.stubs.fw.asi.OpResult
import technology.cariad.entertainment.metadataservice.stubs.fw.asi.OptionalValue
import technology.cariad.entertainment.metadataservice.stubs.fw.asi.OptionalValueOneDimArrayObjectWrapper
import technology.cariad.entertainment.metadataservice.common.log.logger

internal class StorageServiceListener(private val observer: IStorageConnectionObserver) :
    IServiceCallback,
    IStorageServiceListener.Stub() {
    private val log by logger()

    /**
     * Implementation of interface IServiceCallback
     */
    override fun connectionEstablished(serviceName: String, instanceId: Int) {
        log.info(this::connectionEstablished.name) {
            "serviceName: $serviceName " +
                "intanceId: $instanceId"
        }

        observer.onEstablished()
    }

    override fun connectionDisconnected(serviceName: String, instanceId: Int) {
        log.error(this::connectionDisconnected.name) {
            "serviceName: $serviceName " +
                "intanceId: $instanceId"
        }

        observer.onDisconnected()
    }

    override fun error(serviceName: String, instanceId: Int, message: String) {
        log.error(this::error.name) {
            "serviceName: $serviceName " +
                "instanceId: $instanceId " +
                "message: $message"
        }

        observer.onError()
    }

    /**
     * Implementation of interface IStorageServiceListener.Stub()
     */
    override fun publishValue(p0: OptionalValue?) {
        p0?.let {
            val blobID = it.identifier
            val success = it.result == OpResult.op_result_ok

            if (success) {
                log.info(this::publishValue.name) {
                    "blobID: $blobID " +
                        "blobValue.size = ${it.value.size}"
                }

                observer.blobSubscribed(blobID, blobValue = it.value)
            } else {
                log.warn(this::publishValue.name) {
                    "blobID: $blobID " +
                        "result = ${it.result}"
                }
            }

        } ?: log.warn(this::publishValue.name) {
            "p0 = null"
        }
    }

    override fun setReply(p0: Int, p1: OpResult?) {
        p1?.let {
            val result = it == OpResult.op_result_ok
            log.info(this::setReply.name) {
                "blobID: $p0 " +
                    "result: $it"
            }

            observer.resultBlobSet(blobID = p0, result)
        } ?: log.warn(this::setReply.name) {
            "p1 = null"
        }
    }

    override fun get0Reply(p0: OptionalValue?) {
        p0?.let {
            val blobID = it.identifier
            val success = it.result == OpResult.op_result_ok

            if (success) {
                log.info(this::get0Reply.name) {
                    "blobID: $blobID " +
                        "blobValue.size = ${it.value.size}"
                }

                observer.blobRequested(blobID, blobValue = it.value)
            } else {
                log.warn(this::get0Reply.name) {
                    "blobID: $blobID " +
                        "result = ${it.result}"
                }
            }

        } ?: log.warn(this::get0Reply.name) {
            "p0 = null"
        }
    }

    override fun get1Reply(p0: OptionalValueOneDimArrayObjectWrapper?) {
        p0?.let { blobArray ->
            blobArray.array.forEach {
                val blobID = it.identifier
                val success = it.result == OpResult.op_result_ok

                if (success) {
                    log.info(this::get1Reply.name) {
                        "blobID: $blobID " +
                            "blobValue.size = ${it.value.size}"
                    }

                    observer.blobRequested(blobID, blobValue = it.value)
                } else {
                    log.warn(this::get1Reply.name) {
                        "blobID: $blobID " +
                            "result = ${it.result}"
                    }
                }
            }
        } ?: log.warn(this::get1Reply.name) {
            "p0 = null"
        }
    }
}
