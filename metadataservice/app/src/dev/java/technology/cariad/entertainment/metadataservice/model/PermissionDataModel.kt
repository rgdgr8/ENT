package technology.cariad.entertainment.metadataservice.model

import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.service.ServiceHolder
import technology.cariad.entertainment.metadataserviceclient.api.PermissionData
import io.reactivex.rxjava3.disposables.Disposable
import java.util.Observable

class PermissionDataModel(private val serviceHolder: ServiceHolder) : Observable() {

    private val log by logger()

    private var permissionData: PermissionData? = null

    private var request: Disposable? = null

    /**
     * Request actual permission information.
     * Display cached [PermissionData] object fields.
     */
    fun getPermissionInformation() {
        request?.dispose()

        serviceHolder.instance?.let { service ->

            val single = service.getPermissionData()

            log.info(this::getPermissionInformation.name) { "getPermissionData is triggered" }

            request = single.subscribe({

                updateAndNotify(it)
                request = null

                log.info(this::getPermissionInformation.name) {
                    "onNext: fetching permissionData: $permissionData"
                }

            }, {
                updateAndNotify(null)
                request = null

                log.info(this::getPermissionInformation.name) {
                    "onError: error received: ${it.message}"
                }
            })
        }
    }

    /**
     * Dispose the actual request and all cached data
     */
    fun disposePermissionDataRequest() {
        request?.dispose()
        request = null

        updateAndNotify(null)

        log.info(this::disposePermissionDataRequest.name) {
            "get permission data request is disposed"
        }
    }

    /**
     * Retrieve currently fetched permission information
     * @return A [PermissionData] object containing current permission information.
     */
    fun getValue(): PermissionData? {
        return permissionData
    }

    private fun updateAndNotify(newPermissions: PermissionData?) {
        permissionData = newPermissions
        setChanged()
        notifyObservers(permissionData)
    }

    /**
     * Reset the request and permission data cached
     */
    fun reset() {
        permissionData = null
        request = null

        setChanged()
        notifyObservers(null)
    }
}

