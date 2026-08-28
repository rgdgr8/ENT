/*
 * Copyright (c) 2019-2025 e.solutions GmbH
 * Copyright (c) 2026 Cariad SE
 * All rights reserved.
 *
 * Originally developed by e.solutions GmbH
 * Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
 */
package technology.cariad.entertainment.metadataservice.common.disablereasons

import technology.cariad.entertainment.metadataservice.common.log.IRCLogger
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.BACKEND_REASON_GROUP
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.CONFIGURATION_REASON_GROUP
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.CONNECTIVITY_REASON_GROUP
import technology.cariad.entertainment.metadataservice.common.log.IRCLogger.LICENSE_REASON_GROUP
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
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import de.esolutions.coreservices.csm.service.systemservice.DisableReasons as DisableReasonsExt

class DisableReasonsClient(
    private val disableReasonsApiClient: DisableReasonsApiClient,
    private val dispatcher: CoroutineDispatcher,
    private val configurationProvider: IConfigurationProvider
) {
    private val log by logger()

    private val mutableStateFlow = MutableStateFlow<List<DisableReasons>>(listOf())
    private val stateFlow = mutableStateFlow.asStateFlow()

    private val fakeDisabledReasons = listOf(
        listOf(
            DisableReasons(
                ServiceIdentifier.GRACENOTE_V1,
                false,
                listOf(), listOf(), listOf(), listOf()
            ),
            DisableReasons(
                ServiceIdentifier.CONNECTEDRADIO_V2,
                false,
                listOf(), listOf(), listOf(), listOf()
            ), DisableReasons(
                ServiceIdentifier.HYBRIDRADIODATA_V3,
                false,
                listOf(), listOf(), listOf(), listOf()
            ), DisableReasons(
                ServiceIdentifier.GRACENOTEGMD_V1,
                false,
                listOf(), listOf(), listOf(), listOf()
            ), DisableReasons(
                ServiceIdentifier.GRACENOTELYRICS_V1,
                false,
                listOf(), listOf(), listOf(), listOf()
            )
        )
    )

    private var job = subscribeDisabledReasons(false)

    internal suspend fun fakeDisabledReasons(fake: Boolean) {
        unsubscribeDisabledReasons()
        job = subscribeDisabledReasons(fake)
    }

    private fun subscribeDisabledReasons(fake: Boolean): Job {
        return if (fake) {
            fakeDisabledReasons.asFlow().onEach {
                log.info("subscribeDisabledReasons_onEach") {
                    "emitting fake disabled reasons with size=${fakeDisabledReasons.size} entries"
                }
                mutableStateFlow.emit(it)
            }.launchIn(GlobalScope + dispatcher)
        } else {
            disableReasonsApiClient.asFlow().map { filterByName(it) }.map { mapEntries(it) }
                .onEach {
                    writeIrc(it)
                }.onEach { mutableStateFlow.emit(it) }.catch { mutableStateFlow.emit(listOf()) }
                .launchIn(GlobalScope + dispatcher)
        }
    }

    private suspend fun unsubscribeDisabledReasons() {
        job.cancel()
        job.join()
    }

    /**
     * Returns a flow of DisableReasons for the given ServiceIdentifier.
     */
    fun disableReasonsFlow(): StateFlow<List<DisableReasons>> {
        return stateFlow
    }

    /**
     * Checks the current status of available disable Reasons for the given service.
     * @return false if disable reasons shouldn't be evaluated or service doesn't have any
     * disable reasons, true if service has any disable reasons or a RemoteException occurs.
     */
    suspend fun hasAnyDisableReasons(serviceId: ServiceIdentifier): Boolean {
        if (skipCheck(serviceId)) {
            return false
        }

        if (!configurationProvider.getDisableReasonsCheckEnabled()) {
            log.info(this::hasAnyDisableReasons.name) {
                "check disabled by configuration: serviceId=${serviceId.serviceName}"
            }
            return false
        }

        return withContext(dispatcher) {
            disableReasonsApiClient.hasAnyDisableReasons(serviceId)
        }.also {
            log.info(this::hasAnyDisableReasons.name) {
                "disable reasons present for serviceId=${serviceId.serviceName}: $it"
            }
        }
    }

    /**
     * Checks the existence of disable reasons for the given service in the cached disable reasons.
     * @return false if disable reasons shouldn't be evaluated or not found, true if any found.
     */
    fun hasDisableReasons(serviceId: ServiceIdentifier): Boolean {
        if (skipCheck(serviceId)) {
            return false
        }

        return with(findDisableReason(serviceId)) {
            if (this != null) {
                listOf(
                    reasonBackend, reasonConfiguration, reasonConnectivity, reasonLicense
                ).map { it.isEmpty() }.any { !it }

            } else {
                true
            }
        }.also {
            log.info(this::hasDisableReasons.name) {
                "disable reasons present for serviceId=${serviceId.serviceName}: $it"
            }
        }
    }

    private fun findDisableReason(serviceId: ServiceIdentifier): DisableReasons? =
        stateFlow.value.find { it.serviceId == serviceId }

    private fun filterByName(disableReasons: List<DisableReasonsExt>): List<DisableReasonsExt> =
        disableReasons.filter { entry ->
            ServiceIdentifier.values().find { id ->
                id.serviceName == entry.serviceId
            } != null
        }

    private fun skipCheck(serviceId: ServiceIdentifier): Boolean {
        if (!configurationProvider.getDisableReasonsCheckEnabled()) {
            log.debug(this::skipCheck.name) {
                "check disabled by configuration: serviceId=${serviceId.serviceName}"
            }
            return true
        }
        return false
    }

    private fun anyDisableReasons(disableReasons: DisableReasonsExt?): Boolean {
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

    private fun mapEntries(list: List<DisableReasonsExt>): List<DisableReasons> =
        list.map { reason ->
            DisableReasons(
                ServiceIdentifier.values().first { id ->
                    id.serviceName == reason.serviceId
                },
                anyDisableReasons(reason),
                // Fields are int[] arrays - convert to List<Int>
                reasonConnectivity = reason.reasonConnectivity?.toList() ?: emptyList(),
                reasonConfiguration = reason.reasonConfiguration?.toList() ?: emptyList(),
                reasonBackend = reason.reasonBackend?.toList() ?: emptyList(),
                reasonLicense = reason.reasonLicense?.toList() ?: emptyList()
            )
        }

    private fun writeIrc(disableReasons: List<DisableReasons>) {
        disableReasons.filter { it.hasDisableReasons }.onEach { disableReason ->
            listOf(
                Pair(BACKEND_REASON_GROUP, disableReason.reasonBackend),
                Pair(CONFIGURATION_REASON_GROUP, disableReason.reasonConfiguration),
                Pair(CONNECTIVITY_REASON_GROUP, disableReason.reasonConnectivity),
                Pair(LICENSE_REASON_GROUP, disableReason.reasonLicense)
            ).filter { reason ->
                reason.second.isNotEmpty()
            }.also { list ->
                val disableReasonGroups = list.joinToString {
                    it.first + "|"
                }
                val disableReasonsString = list.joinToString {
                    it.second.toString() + "|"
                }
                IRCLogger.appServiceListError(
                    disableReason.serviceId.serviceName,
                    disableReasonGroups,
                    disableReasonsString
                )
            }
        }
    }
}

