package technology.cariad.entertainment.metadataservice.common.disablereasons

import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier

data class DisableReasons(
    val serviceId: ServiceIdentifier,
    val hasDisableReasons: Boolean,
    var reasonConnectivity: List<Int>,
    var reasonConfiguration: List<Int>,
    var reasonBackend: List<Int>,
    var reasonLicense: List<Int>
)

