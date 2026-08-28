package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.WebStream
import de.esolutions.connectedradioclient.data.enums.StreamQuality
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity

class StationStreamMapper : Mapper<WebStream, StationStreamEntity>() {
    override fun mapValue(value: WebStream): StationStreamEntity {

        val quality = when(value.streamQuality) {
            StreamQuality.HIGH -> StationStreamEntity.Companion.QualityType.HIGH
            StreamQuality.LOW -> StationStreamEntity.Companion.QualityType.LOW
            else -> StationStreamEntity.Companion.QualityType.UNKNOWN
        }

        return StationStreamEntity(
            value.url,
            value.offsetMillis,
            quality
        )
    }
}
