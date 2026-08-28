package technology.cariad.entertainment.metadataservice.model

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataserviceclient.api.StationAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable

class StationAsiaModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()

    private var stationAsia: StationAsia? = null

    private var request: Disposable? = null

    /**
     * Request the station for Asia
     * @param stationBearerAsia the bearer for Asia
     */
    fun requestStation(stationBearerAsia: StationBearerAsia) {
        serviceHolder.instance?.let { service ->
            val single = service.observeRadioStationDataAsia(stationBearerAsia)
            log.info(this::requestStation.name) {
                "request data for bearer:" +
                        "type=${stationBearerAsia.type} " +
                        "frequency=${stationBearerAsia.frequencyKhz} " +
                        "mcc=${stationBearerAsia.mcc}"
            }

            request = single.subscribe({ station ->
                log.info(this::requestStation.name) { "onSuccess: station data received" }

                stationAsia = station
                setChanged()
                notifyObservers(station)
            }, {
                log.info(this::requestStation.name) { "onError: request failed ${it.message}" }
            })
        }
    }

    /**
     * Stop the request and resets the station
     */
    fun disposeRequest() {
        stationAsia = null
        request?.let {
            setChanged()
            notifyObservers(null)
            it.dispose()
        }

        log.info(this::disposeRequest.name) {
            "request disposed"
        }
    }

    /**
     * Getter of the Asio station
     * @return Returns a Parceable of the station
     */
    fun getStation(): StationAsia? {
        return stationAsia
    }

    /**
     * Resets the station
     */
    fun reset() {
        stationAsia = null
        request = null

        setChanged()
        notifyObservers(null)
    }
}
