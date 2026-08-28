package technology.cariad.entertainment.metadataservice.common.servicelist

import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier

/**
 * Relevant values from ServiceList
 */
data class ServiceListEntry(
    val serviceId: ServiceIdentifier,
    val enabled: Boolean,
    val apn: Apn,
    val isTetheringAllowed: Boolean,
    val defaultHttpOperation: HttpOperation?,
    val httpOperations: Map<String, HttpOperation>?
)

