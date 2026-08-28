package technology.cariad.entertainment.metadataservice.model

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.common.util.fromBearerString
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataservice.service.mapper.BearerEntityMapper
import technology.cariad.entertainment.metadataserviceclient.api.Station
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable
import java.util.Observer

/**
 * A simple class providing station data from a given MetadataService instance.
 * A station can be requested by bearer and the result is cached internally.
 * Clients of this class can register for changes of the cache via [Observable.addObserver].
 * If only a single station is modified, the bearer Uri it was requested with is passed as arg to [Observer.update].
 * Otherwise no additional data is included in the update.
 * Getters are provided for to retrieve the currently cached data.
 * @param serviceHolder An already connected and registered service instance.
 */
class StationModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()

    //station data cache
    private val stationsData = mutableListOf<Pair<String, Station?>>()

    //open requests
    private val requests = mutableMapOf<String, Disposable>()

    /**
     * Retrieve cached station data by bearer.
     * @param bearer String containing a bearer Uri.
     * @return A [Station] object containing all available information or null if the bearer is not known.
     */
    fun getStation(bearer: String): Station? {
        return stationsData.find { it.first == bearer }?.second
    }

    /**
     * Retrieve a cached list of all available stations.
     * @return The list of [Station] objects paired with the bearer of the request.
     */
    fun getStations(): List<Pair<String, Station?>> {
        return stationsData
    }

    /**
     * Request station data for a bearer.
     * The same bearer can't be added multiple times.
     * Once the state of the
     * @param bearer String containing a bearer Uri.
     */
    fun addStation(bearer: String) {

        if (requests.containsKey(bearer)) {
            log.info(this::addStation.name) { "bearer=$bearer already exists" }
            return
        }

        val bearerEntity: IBearerEntity = fromBearerString(bearer) ?: return
        val rawBearer = BearerEntityMapper().mapValue(bearerEntity)

        stationsData.add(
            Pair(bearer, Station(null, null, null, listOf(), listOf(), "", listOf()))
        )
        setChanged()
        notifyObservers(bearer)

        serviceHolder.instance?.let { service ->
            val observable = service.observeRadioStationData(rawBearer, "")

            requests[bearer] = observable.subscribe({ station ->
                log.info(this::addStation.name) { "onNext: data received for bearer=$bearer" }

                val index = stationsData.indexOfFirst { it.first == bearer }
                if (index >= 0) {
                    stationsData[index] = Pair(bearer, station)
                    setChanged()
                    notifyObservers(bearer)
                } else {
                    log.info(this::addStation.name) {
                        "onNext ignored: bearer=$bearer no longer in cache"
                    }
                }

            }, {
                log.info(this::addStation.name) {
                    "onError: error received for bearer=$bearer ${it.message}"
                }
            }, {
                log.info(this::addStation.name) { "onComplete: request completed for bearer=$bearer" }
            })

            log.info(this::addStation.name) { "bearer=$bearer added" }
        }
    }

    /**
     * Cancel the request for a bearer.
     * @param bearer String containing a bearer Uri
     */
    fun removeStation(bearer: String) {
        requests.remove(bearer)?.dispose()
        stationsData.removeIf { it.first == bearer }
        setChanged()
        notifyObservers(bearer)

        log.info(this::removeStation.name) { "bearer=$bearer" }
    }

    /**
     * Resets the station data
     */
    fun reset() {
        requests.clear()
        stationsData.clear()

        setChanged()
        notifyObservers(null)
    }
}

