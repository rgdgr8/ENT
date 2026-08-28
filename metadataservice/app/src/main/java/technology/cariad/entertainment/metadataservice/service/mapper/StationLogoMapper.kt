package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataserviceclient.api.StationLogo

class StationLogoMapper : Mapper<ImageEntity, StationLogo>() {
    override fun mapValue(value: ImageEntity): StationLogo = StationLogo(
        value.uri!!
    )
}
