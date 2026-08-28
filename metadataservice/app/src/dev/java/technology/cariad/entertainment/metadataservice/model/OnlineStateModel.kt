package technology.cariad.entertainment.metadataservice.model

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable

class OnlineStateModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()

    private var onlineLookupAllowed = true

    private var request: Disposable? = null

    /**
     * Change de current status of the Online look up
     * @param enabled New status
     */
    fun changeOnlineLookupState(enabled: Boolean) {
        serviceHolder.instance?.let { service ->

            val single = service.getRecordingMetadataByName("Cruel Summer", "Taylor Swift")

            log.info(this::changeOnlineLookupState.name) { "enabled=$enabled" }

            request = single.subscribe({

                log.info(this::changeOnlineLookupState.name) { "onNext: online state enabled=$it" }

                onlineLookupAllowed = true//it
                setChanged()
                notifyObservers(onlineLookupAllowed)

            }, {
                log.info(this::changeOnlineLookupState.name) {
                    "onError: error received: ${it.message}"
                }
            })
        }
    }

    /**
     * Returns the Online look up state
     * @return actual state
     */
    fun getOnlineLookupState(): Boolean {
        return onlineLookupAllowed
    }

    /**
     * Resets the current request
     */
    fun reset() {
        onlineLookupAllowed = true
        request = null

        setChanged()
        notifyObservers(onlineLookupAllowed)
    }
}

