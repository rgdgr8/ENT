/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 */
package technology.cariad.entertainment.metadataservice.common.disablereasons

import android.content.Context
import android.os.RemoteException
import de.esolutions.coreservices.csm.service.systemservice.DisableReasons
import de.esolutions.coreservices.csm.service.systemservice.DisableReasonsApiCallback
import de.esolutions.magicadapter.stapi.DisableReasonsApiBinding
import de.esolutions.magicadapter.stapi.ServiceConnectionCallback
import de.esolutions.magicadapter.stapi.ServiceConnectionFailure
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DisableReasonsApiClient(
    context: Context
) {
    private val log by logger()

    private val disableReasonApiBinding = DisableReasonsApiBinding(context)

    private val clientName = context.packageName
    
    // Use MutableStateFlow to buffer values (fixes timing issues with callbackFlow)
    private val _disableReasonsFlow = MutableStateFlow<List<DisableReasons>>(emptyList())
    
    private var isConnected = false

    internal fun asFlow(): StateFlow<List<DisableReasons>> {
        if (!isConnected) {
            connectToService()
        }
        return _disableReasonsFlow.asStateFlow()
    }
    
    private fun connectToService() {
        log.info("connectToService") { "Connecting to DisableReasonsApi..." }
        val serviceConnectionCallback = initServiceConnection()
        disableReasonApiBinding.connect(serviceConnectionCallback, Context.BIND_AUTO_CREATE)
    }

    internal fun hasAnyDisableReasons(
        serviceIdentifier: ServiceIdentifier
    ): Boolean = try {
        disableReasonApiBinding.hasDisableReasons(serviceIdentifier.serviceName)
    } catch (e: RemoteException) {
        log.error(this::hasAnyDisableReasons.name) {
            "exception caught: RemoteException=${e.message}"
        }
        true
    }

    private fun initDisableReasonApiCallback(): DisableReasonsApiCallback.Stub =
        object : DisableReasonsApiCallback.Stub() {
            override fun updateDisableReasons(disableReasons: List<DisableReasons>?) {
                log.info(this::updateDisableReasons.name) {
                    "received disable reasons list with size=${disableReasons?.size ?: 0}"
                }

                val reasonsOrEmpty = disableReasons ?: listOf()
                if (disableReasons != null) {
                    printDisableReasons(disableReasons)
                }
                
                _disableReasonsFlow.value = reasonsOrEmpty
                log.info("updateDisableReasons_emit") {
                    "emitted to StateFlow: size=${reasonsOrEmpty.size}"
                }
            }
        }

    private fun initServiceConnection(): ServiceConnectionCallback =
        object : ServiceConnectionCallback {
            override fun serviceConnected() {
                log.info(this::serviceConnected.name) { "service connected" }
                isConnected = true

                val callback = initDisableReasonApiCallback()
                disableReasonApiBinding.registerDisableReasonsApiCallback(callback, clientName)
            }

            override fun serviceDisconnected(failureReason: ServiceConnectionFailure?) {
                log.warn(this::serviceConnected.name) {
                    "service disconnected: Reason = $failureReason"
                }
                isConnected = false
                _disableReasonsFlow.value = listOf()
            }
        }

    private fun printDisableReasons(disableReasons: List<DisableReasons>) {
        val sb = StringBuilder()
        disableReasons.forEach { 
            // Use public field access (device uses public fields, not getters)
            sb.append(it.serviceId)
              .append(" hasDisable=")
              .append(anyDisableReasons(it))
              .append("\n") 
        }
        log.info(this::printDisableReasons.name) { "disable reasons:\n$sb" }
    }

    private fun anyDisableReasons(disableReasons: DisableReasons?): Boolean {
        return with(disableReasons) {
            if (this != null) {
                // Fields are now int[] arrays, not List<Integer>
                listOf(
                    reasonBackend?.isNotEmpty() ?: false,
                    reasonConfiguration?.isNotEmpty() ?: false,
                    reasonConnectivity?.isNotEmpty() ?: false,
                    reasonLicense?.isNotEmpty() ?: false
                ).any { it }
            } else {
                true
            }
        }
    }
}

