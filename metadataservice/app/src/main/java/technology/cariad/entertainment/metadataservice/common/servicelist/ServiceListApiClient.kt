/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 */
package technology.cariad.entertainment.metadataservice.common.servicelist

import android.content.Context
import de.esolutions.coreservices.csm.service.systemservice.ServiceListApiCallback
import de.esolutions.coreservices.csm.service.systemservice.ServiceListEntry
import de.esolutions.magicadapter.stapi.ServiceConnectionCallback
import de.esolutions.magicadapter.stapi.ServiceConnectionFailure
import de.esolutions.magicadapter.stapi.ServiceListApiBinding
import technology.cariad.entertainment.metadataservice.common.log.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServiceListApiClient(
    context: Context
) {
    private val log by logger()

    private val serviceListApiBinding = ServiceListApiBinding(context)

    private val clientName = context.packageName
    
    // Use StateFlow to buffer values - will replay latest value to new collectors
    private val _serviceListFlow = MutableStateFlow<List<ServiceListEntry>>(emptyList())
    
    private var isConnected = false

    internal fun asFlow(): Flow<List<ServiceListEntry>> {
        if (!isConnected) {
            log.info("asFlow") { "Connecting to ServiceListApi..." }
            val serviceConnectionCallback = initServiceConnection()
            serviceListApiBinding.connect(serviceConnectionCallback, Context.BIND_AUTO_CREATE)
            isConnected = true
        }
        return _serviceListFlow.asStateFlow()
    }

    private fun initServiceListApiCallback(): ServiceListApiCallback.Stub {
        return object : ServiceListApiCallback.Stub() {
            override fun updateServiceList(services: List<ServiceListEntry>?) {
                log.info(this::updateServiceList.name) {
                    "received service list with size=${services?.size ?: 0}"
                }

                val servicesOrEmpty = services ?: listOf()
                _serviceListFlow.value = servicesOrEmpty
                log.info("updateServiceList_emit") {
                    "emitted to StateFlow: size=${servicesOrEmpty.size}"
                }
                // printServiceList removed - serviceID field not available on device
            }
        }
    }

    private fun initServiceConnection(): ServiceConnectionCallback {
        return object : ServiceConnectionCallback {
            override fun serviceConnected() {
                log.info(this::serviceConnected.name) { "service connected" }

                val callback = initServiceListApiCallback()
                serviceListApiBinding.registerServiceListApiCallback(callback, clientName)
            }

            override fun serviceDisconnected(failureReason: ServiceConnectionFailure?) {
                log.warn(this::serviceConnected.name) {
                    "service disconnected: Reason = $failureReason"
                }
                _serviceListFlow.value = listOf()
                isConnected = false
            }
        }
    }
}

