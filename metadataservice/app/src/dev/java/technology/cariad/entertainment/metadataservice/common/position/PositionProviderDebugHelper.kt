package technology.cariad.entertainment.metadataservice.common.position

import technology.cariad.entertainment.metadataservice.domain.IConfigurationProvider

@Suppress("SwallowedException")
internal fun applyDebugPosition(
    positionInformation: PositionInformation?,
    configurationProvider: IConfigurationProvider
): PositionInformation? {
    val positionOverride = configurationProvider.getPositionOverride()
    return if (positionOverride.isNotEmpty()) {
        try {
            val latLong = positionOverride.split(";")
            val lat = latLong[0].toDouble()
            val long = latLong[1].toDouble()
            PositionInformation(lat, long)
        } catch (E: Exception) {
            positionInformation
        }
    } else {
        positionInformation
    }
}

