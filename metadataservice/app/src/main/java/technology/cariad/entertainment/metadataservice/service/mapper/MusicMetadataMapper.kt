package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.MusicMetadataEntity
import technology.cariad.entertainment.metadataserviceclient.api.MetadataError
import technology.cariad.entertainment.metadataserviceclient.api.MusicMetadata

class MusicMetadataMapper : Mapper<MusicMetadataEntity, MusicMetadata>() {
    override fun mapValue(value: MusicMetadataEntity): MusicMetadata =
        MusicMetadata(
            content = value.content,
            error = MetadataError(
                ErrorTypeMapper().mapValue(value.errorType),
                value.statusCode,
                value.errorMessage
            )
        )
}

