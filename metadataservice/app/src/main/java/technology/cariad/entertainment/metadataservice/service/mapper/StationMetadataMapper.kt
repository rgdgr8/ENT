package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import technology.cariad.entertainment.metadataserviceclient.api.StationMetadata

class StationMetadataMapper : Mapper<StationMetadataEntity, StationMetadata>() {
    override fun mapValue(value: StationMetadataEntity): StationMetadata = StationMetadata(
        value.shortName,
        value.mediumName,
        value.longName,
        value.description,
        value.genres
    )
}
