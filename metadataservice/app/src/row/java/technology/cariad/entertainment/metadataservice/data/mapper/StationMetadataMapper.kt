package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.StationMetadata
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity

class StationMetadataMapper : Mapper<StationMetadata, StationMetadataEntity>() {
    override fun mapValue(value: StationMetadata): StationMetadataEntity = StationMetadataEntity(
        value.shortName,
        value.mediumName,
        value.longName,
        value.description,
        value.genres,
        null
    )
}
