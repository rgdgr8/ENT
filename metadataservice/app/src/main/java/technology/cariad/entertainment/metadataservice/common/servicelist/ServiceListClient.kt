/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 */
package technology.cariad.entertainment.metadataservice.common.servicelist

import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.common.log.logger
import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.plus
import de.esolutions.coreservices.csm.service.systemservice.ServiceListEntry as ServiceListEntryExt

class ServiceListClient(
    private val serviceListApiClient: ServiceListApiClient,
    private val configurationProvider: IConfigurationProvider,
    private val dispatcher: CoroutineDispatcher
) : IServiceListClient {
    private val log by logger()

    private val mutableStateFlow = MutableStateFlow<List<ServiceListEntry>>(listOf())
    private val stateFlow = mutableStateFlow.asStateFlow()

    private val fakeServiceList = listOf(
        listOf(
            ServiceListEntry(
                ServiceIdentifier.GRACENOTE_V1,
                true,
                Apn.APN_DLINK,
                false,
                null,
                null
            ),
            ServiceListEntry(
                ServiceIdentifier.CONNECTEDRADIO_V2,
                true,
                Apn.APN_DLINK,
                false,
                HttpOperation(
                    "DEFAULT",
                    BuildConfig.DEFAULT_CONRAD_BACKEND_URL,
                    "sc0:eso"
                ),
                null
            ),
            ServiceListEntry(
                ServiceIdentifier.HYBRIDRADIODATA_V3,
                true,
                Apn.APN_DLINK,
                false,
                null,
                null
            ),
            ServiceListEntry(
                ServiceIdentifier.GRACENOTELYRICS_V1,
                true,
                Apn.APN_DLINK,
                false,
                null,
                null
            ),
            ServiceListEntry(
                ServiceIdentifier.GRACENOTEGMD_V1,
                true,
                Apn.APN_DLINK,
                false,
                null,
                mapOf(
                    "RECORDINGS_STRING_SEARCH" to HttpOperation(
                        "RECORDINGS_STRING_SEARCH",
                        "https://api.gmd.music.gracenote.com/v3/recordings/search?" +
                                "recordingName=\$(recordingName)&artistName=\$(artistName)\n",
                        "ece:gmd:service:t1:a"
                    ),
                    "ARTISTS" to HttpOperation(
                        "ARTISTS",
                        "https://api.gmd.music.gracenote.com/v3/artists/lookup?" +
                                "ids=\$(gnid)&idType=artistID\n",
                        "ece:gmd:service:t1:a"
                    ),
                    "RECORDINGS" to HttpOperation(
                        "RECORDINGS",
                        "https://api.gmd.music.gracenote.com/v3/recordings/lookup?" +
                                "ids=\$(gnid)&idType=recordingID\n",
                        "ece:gmd:service:t1:a"
                    ),
                    "ALBUMS" to HttpOperation(
                        "ALBUMS",
                        "https://api.gmd.music.gracenote.com/v3/albumEditions/lookup?" +
                                "ids=\$(gnid)&idType=albumEditionID\n",
                        "ece:gmd:service:t1:a"
                    )
                )
            ),
        )
    )

    private var job = subscribeServiceList(false)

    /**
     * @see [IServiceListClient.serviceListFlow]
     */
    override fun serviceListFlow(): StateFlow<List<ServiceListEntry>> {
        return stateFlow
    }

    /**
     * @see [IServiceListClient.serviceEnabled]
     */
    override fun serviceEnabled(serviceId: ServiceIdentifier): Boolean {
        if (!configurationProvider.getServiceListCheckEnabled()) {
            log.debug(this::serviceEnabled.name) {
                "check disabled by configuration: serviceId=${serviceId.serviceName}"
            }
            return true
        }

        val serviceList = mutableStateFlow.value
        log.info(this::serviceEnabled.name) {
            "mutableStateFlow.value has ${serviceList.size} entries: ${serviceList.map { it.serviceId }}"
        }

        val entry = serviceList.find { it.serviceId == serviceId }
        val enabled = entry?.enabled ?: false

        log.info(this::serviceEnabled.name) { "$serviceId: enabled=$enabled" }

        return enabled
    }

    internal suspend fun fakeServiceList(fake: Boolean) {
        unsubscribeServiceList()
        job = subscribeServiceList(fake)
    }

    private fun subscribeServiceList(fake: Boolean): Job {
        return if (fake) {
            fakeServiceList.asFlow()
                .map { applyDebugConfig(it, configurationProvider) }
                .onEach {
                    log.info("subscribeServiceList_onEach") {
                        "emitting fake service list with size=${fakeServiceList.size} entries"
                    }
                    mutableStateFlow.emit(it)
                }
                .launchIn(GlobalScope + dispatcher)
        } else {
            log.info("subscribeServiceList") { "STARTING subscription to serviceListApiClient.asFlow()" }
            serviceListApiClient.asFlow()
                .onStart {
                    log.info("subscribeServiceList_onStart") { "Flow subscription started" }
                }
                .onEach { rawList ->
                    log.info("subscribeServiceList_raw") {
                        "received raw service list with size=${rawList.size}"
                    }
                    // Log ALL fields of each entry using reflection
                    rawList.forEachIndexed { index, entry ->
                        val sb = StringBuilder()
                        sb.append("RAW_ENTRY[$index]: ")
                        entry.javaClass.declaredFields.forEach { field ->
                            field.isAccessible = true
                            try {
                                val value = field.get(entry)
                                sb.append("${field.name}=$value, ")
                            } catch (e: Exception) {
                                sb.append("${field.name}=ERROR(${e.message}), ")
                            }
                        }
                        log.info("subscribeServiceList_rawEntry") { sb.toString() }
                        
                        // Log httpOperations array contents
                        try {
                            val httpOpsField = entry.javaClass.getDeclaredField("httpOperations")
                            httpOpsField.isAccessible = true
                            val httpOps = httpOpsField.get(entry)
                            if (httpOps != null && httpOps.javaClass.isArray) {
                                val length = java.lang.reflect.Array.getLength(httpOps)
                                log.info("subscribeServiceList_httpOps") { 
                                    "ENTRY[$index] serviceID=${entry.serviceID} has $length httpOperations:" 
                                }
                                for (i in 0 until length) {
                                    val httpOp = java.lang.reflect.Array.get(httpOps, i)
                                    if (httpOp != null) {
                                        val opSb = StringBuilder()
                                        opSb.append("  httpOp[$i]: ")
                                        httpOp.javaClass.declaredFields.forEach { opField ->
                                            opField.isAccessible = true
                                            try {
                                                val opValue = opField.get(httpOp)
                                                opSb.append("${opField.name}=$opValue, ")
                                            } catch (e: Exception) {
                                                opSb.append("${opField.name}=ERROR, ")
                                            }
                                        }
                                        log.info("subscribeServiceList_httpOp") { opSb.toString() }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            log.warn("subscribeServiceList_httpOps") { "Error reading httpOperations: ${e.message}" }
                        }
                    }
                }
                .map { filterByName(it) }
                .onEach { filteredList ->
                    log.info("subscribeServiceList_filtered") {
                        "filtered service list with size=${filteredList.size}"
                    }
                }
                .map { mapEntries(it) }
                .onEach { mappedList ->
                    log.info("subscribeServiceList_mapped") {
                        "mapped service list with size=${mappedList.size}: ${mappedList.map { "${it.serviceId}=${it.enabled}" }}"
                    }
                }
                .map { applyDebugConfig(it, configurationProvider) }
                .onEach { mutableStateFlow.emit(it) }
                .catch { e ->
                    log.error("subscribeServiceList_catch") { "Error in flow: ${e.message}" }
                    mutableStateFlow.emit(listOf())
                }
                .launchIn(GlobalScope + dispatcher)
        }
    }

    private suspend fun unsubscribeServiceList() {
        job.cancel()
        job.join()
    }

    private fun filterByName(serviceList: List<ServiceListEntryExt>): List<ServiceListEntryExt> =
        serviceList.filter { entry ->
            ServiceIdentifier.values().find { id ->
                id.serviceName.equals(entry.serviceID, ignoreCase = true)
            } != null
        }

    // Helper function to read HttpOperation fields via reflection (device uses public fields, not getters)
    private fun readHttpOpUrl(httpOp: Any): String = try {
        val urlField = httpOp.javaClass.getDeclaredField("url")
        urlField.isAccessible = true
        urlField.get(httpOp) as? String ?: ""
    } catch (e: Exception) { "" }
    
    private fun readHttpOpOperation(httpOp: Any): Any? = try {
        val opField = httpOp.javaClass.getDeclaredField("operation")
        opField.isAccessible = true
        opField.get(httpOp)
    } catch (e: Exception) { null }
    
    private fun readOperationId(operation: Any?): String {
        if (operation == null) return "DEFAULT"
        return try {
            val idField = operation.javaClass.getDeclaredField("id")
            idField.isAccessible = true
            idField.get(operation) as? String ?: "DEFAULT"
        } catch (e: Exception) { "DEFAULT" }
    }
    
    private fun readOperationScope(operation: Any?): String {
        if (operation == null) return ""
        return try {
            val authField = operation.javaClass.getDeclaredField("authentication")
            authField.isAccessible = true
            val auth = authField.get(operation)
            if (auth != null && auth.javaClass.isArray && java.lang.reflect.Array.getLength(auth) > 0) {
                val firstAuth = java.lang.reflect.Array.get(auth, 0)
                val scopeField = firstAuth.javaClass.getDeclaredField("scope")
                scopeField.isAccessible = true
                scopeField.get(firstAuth) as? String ?: ""
            } else ""
        } catch (e: Exception) { "" }
    }

    private fun mapEntries(serviceList: List<ServiceListEntryExt>): List<ServiceListEntry> =
        serviceList.map { entry ->
            // Use reflection to access httpOperations - it's an Array on device, not List
            val httpOpsList: List<Any>? = try {
                val httpOpsField = entry.javaClass.getDeclaredField("httpOperations")
                httpOpsField.isAccessible = true
                val httpOps = httpOpsField.get(entry)
                if (httpOps != null && httpOps.javaClass.isArray) {
                    val length = java.lang.reflect.Array.getLength(httpOps)
                    (0 until length).mapNotNull { i ->
                        java.lang.reflect.Array.get(httpOps, i)
                    }
                } else if (httpOps is List<*>) {
                    httpOps.filterNotNull()
                } else {
                    null
                }
            } catch (e: Exception) {
                log.warn("mapEntries") { "Error accessing httpOperations: ${e.message}" }
                null
            }
            
            val firstHttpOpUrl = httpOpsList?.firstOrNull()?.let { readHttpOpUrl(it) }
            log.info("mapEntries") { 
                "entry=${entry.serviceID}: httpOpsList size=${httpOpsList?.size ?: 0}, firstUrl=$firstHttpOpUrl" 
            }
            
            ServiceListEntry(
                ServiceIdentifier.values().first { id ->
                    id.serviceName.equals(entry.serviceID, ignoreCase = true)
                },
                entry.enableDisableServiceState,
                mapServiceListApn(entry.apn),
                entry.tetheringAllowed,
                httpOpsList?.firstOrNull()?.let { httpOp ->
                    val operation = readHttpOpOperation(httpOp)
                    val opId = readOperationId(operation)
                    val scope = readOperationScope(operation)
                    val url = readHttpOpUrl(httpOp)
                    
                    HttpOperation(opId, url, scope)
                },
                httpOpsList?.associate { httpOp ->
                    val operation = readHttpOpOperation(httpOp)
                    val opId = readOperationId(operation)
                    val scope = readOperationScope(operation)
                    val url = readHttpOpUrl(httpOp)
                    
                    opId to HttpOperation(opId, url, scope)
                }
            )
        }
}

