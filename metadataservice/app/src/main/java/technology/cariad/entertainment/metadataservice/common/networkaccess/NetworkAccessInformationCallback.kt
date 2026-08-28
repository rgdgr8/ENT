package technology.cariad.entertainment.metadataservice.common.networkaccess

import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlinx.coroutines.channels.ProducerScope

internal class NetworkAccessInformationCallback(private val producerScope: ProducerScope<Int?>) :
    CarPropertyManager.CarPropertyEventCallback {

    private val log by logger()

    override fun onChangeEvent(value: CarPropertyValue<*>) {
        val mcc = value.value

        if (mcc is Int) {
            log.info(this::onChangeEvent.name) { "mcc=${value.value}" }
            producerScope.trySend(mcc)
        } else {
            log.error(this::onChangeEvent.name) {
                "type mismatch: expected Int, got ${mcc.javaClass}"
            }
        }
    }

    override fun onErrorEvent(propertyId: Int, areaId: Int) {
        log.error("onErrorEvent") {
            "propertyId=$propertyId, areaId=$areaId"
        }
    }

    override fun onErrorEvent(propertyId: Int, areaId: Int, errorCode: Int) {
        log.error("onErrorEvent") {
            "propertyId=$propertyId, areaId=$areaId errorCode=$errorCode"
        }
    }
}

