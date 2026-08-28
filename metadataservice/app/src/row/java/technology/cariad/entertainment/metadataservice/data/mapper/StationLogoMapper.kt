package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.StationLogo
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity

class StationLogoMapper : Mapper<StationLogo, ImageEntity>() {
    override fun mapValue(value: StationLogo): ImageEntity = ImageEntity(
        null,
        "${value.fileName}.${value.fileExtension}",
        null,
        value.imageData.toList(),
        null
    )
}

