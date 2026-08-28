package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationAsiaEntity

class SampleStationAsia {

    companion object {

        const val STATION_ASIA_NAME_1 = "StationName"
        val STATION_ASIA_IMAGE_DATA_1 = byteArrayOf(0x12, 0x34)
        const val STATION_PROVIDER_ID_1 = "ProviderId"
        const val IMAGE_NAME_UUID = "aaaaaaaa-bbbb-1111-2222-aabbccddeeff"
        val STATION_ASIA_ENTITY_1
            get() = StationAsiaEntity(
                STATION_ASIA_NAME_1,
                ImageEntity(
                    fileName = "$IMAGE_NAME_UUID.jpg",
                    imageData = STATION_ASIA_IMAGE_DATA_1.toList(),
                    imageType = ImageEntity.Companion.ImageType.LOGO
                ),
                ProviderIdentifierEntity(
                    ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                    STATION_PROVIDER_ID_1
                )
            )

        val STATION_ASIA_ENTITY_EMPTY_1
            get() = StationAsiaEntity(
                "",
                null,
                null
            )
    }
}

