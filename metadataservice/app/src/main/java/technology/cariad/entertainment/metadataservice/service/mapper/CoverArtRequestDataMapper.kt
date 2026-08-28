package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData

class CoverArtRequestDataMapper : Mapper<CoverArtRequestData, CoverArtRequestDataEntity>() {
    override fun mapValue(value: CoverArtRequestData): CoverArtRequestDataEntity {
        return CoverArtRequestDataEntity(value.artist, value.title, value.album, null)
    }
}
