package technology.cariad.entertainment.metadataservice.common.networkaccess

import android.car.hardware.property.CarPropertyManager
import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val PROPERTY_MOBILE_COUNTRY_CODE = 557842725

class NetworkAccessProviderApi(
    private val carPropertyManager: CarPropertyManager
) {
    private val log by logger()

    private fun subscribe(carPropertyListener: NetworkAccessInformationCallback): Boolean {

        return try {
            carPropertyManager.registerCallback(
                carPropertyListener,
                PROPERTY_MOBILE_COUNTRY_CODE,
                CarPropertyManager.SENSOR_RATE_ONCHANGE
            )
        } catch (e: Exception) {
            log.info(this::subscribe.name) {
                "failed to register property listener for " +
                        "propertyId=$PROPERTY_MOBILE_COUNTRY_CODE " +
                        "message=${e.message}"
            }
            false
        }.also {
            log.info(this::subscribe.name) {
                "register property listener: success=$it"
            }
        }
    }

    /** Unsubscribe from the VHAL property updates  */
    private fun unsubscribe(carPropertyListener: NetworkAccessInformationCallback) {

        log.info(this::unsubscribe.name) {
            "unsubscribe property listener"
        }

        try {
            carPropertyManager.unregisterCallback(carPropertyListener)
        } catch (e: Exception) {
            log.info(this::subscribe.name) {
                "failed property listener unregister: message=${e.message}"
            }
        }
    }

    internal fun networkAccessFlow(): Flow<Int?> {
        return callbackFlow {
            val apiListener = NetworkAccessInformationCallback(this)

            log.info(this@NetworkAccessProviderApi::networkAccessFlow.name) {
                "connect service"
            }

            subscribe(apiListener)

            awaitClose {
                log.info(this@NetworkAccessProviderApi::networkAccessFlow.name) {
                    "disconnect service"
                }

                unsubscribe(apiListener)
            }
        }
    }
}

