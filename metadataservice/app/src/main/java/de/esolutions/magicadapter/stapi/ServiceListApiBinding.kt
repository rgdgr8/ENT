package de.esolutions.magicadapter.stapi

import android.content.Context
import android.util.Log
import de.esolutions.coreservices.csm.service.systemservice.Apn
import de.esolutions.coreservices.csm.service.systemservice.HttpOperation
import de.esolutions.coreservices.csm.service.systemservice.ServiceListApiCallback
import de.esolutions.coreservices.csm.service.systemservice.ServiceListEntry

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.magicadapter.stapi.ServiceListApiBinding
 * Returns a mock service list with CONNECTEDRADIO_V2 enabled for local testing.
 */
class ServiceListApiBinding(private val context: Context) {
    
    private val tag = "[ServiceListApiBinding-STUB]"
    
    fun connect(callback: ServiceConnectionCallback, flags: Int) {
        Log.i(tag, "connect() - STUB: flags=$flags")
        // Simulate successful connection
        android.os.Handler(context.mainLooper).postDelayed({
            callback.serviceConnected()
        }, 100)
    }
    
    fun disconnect() {
        Log.i(tag, "disconnect() - STUB")
    }
    
    fun registerServiceListApiCallback(callback: ServiceListApiCallback, clientName: String) {
        Log.i(tag, "registerServiceListApiCallback() - STUB: clientName=$clientName")
        // Return mock service list with CONNECTEDRADIO_V2 enabled
        android.os.Handler(context.mainLooper).postDelayed({
            try {
                val serviceList = createMockServiceList()
                Log.i(tag, "Returning mock service list with ${serviceList.size} entries")
                callback.updateServiceList(serviceList)
            } catch (e: Exception) {
                Log.w(tag, "Failed to update service list: ${e.message}")
            }
        }, 100)
    }
    
    fun unregisterServiceListApiCallback(clientName: String) {
        Log.i(tag, "unregisterServiceListApiCallback() - STUB: clientName=$clientName")
    }
    
    private fun createMockServiceList(): List<ServiceListEntry> {
        // Create authentication with scope
        val authentication = de.esolutions.coreservices.csm.service.systemservice.Authentication()
        authentication.type = 1 // Bearer token
        authentication.scope = "sc0:eso"
        
        // Create a mock HTTP operation for the ConnectedRadio service
        val operation = de.esolutions.coreservices.csm.service.systemservice.Operation()
        operation.id = "DEFAULT"
        operation.authentication = listOf(authentication)
        
        val httpOperation = HttpOperation()
        httpOperation.url = "https://connectedradio-v2.approval.car-cloud.org/"
        httpOperation.operation = operation
        
        // CONNECTEDRADIO_V2 service entry - enabled
        val connectedRadioEntry = ServiceListEntry()
        connectedRadioEntry.serviceID = "CONNECTEDRADIO_V2"
        connectedRadioEntry.enableDisableServiceState = true // Enabled
        connectedRadioEntry.apn = Apn.APN_DLINK
        connectedRadioEntry.tetheringAllowed = false
        connectedRadioEntry.httpOperations = listOf(httpOperation)
        
        Log.i(tag, "Created mock service: ${connectedRadioEntry.serviceID} enabled=${connectedRadioEntry.enableDisableServiceState} url=${httpOperation.url}")
        
        return listOf(connectedRadioEntry)
    }
}
