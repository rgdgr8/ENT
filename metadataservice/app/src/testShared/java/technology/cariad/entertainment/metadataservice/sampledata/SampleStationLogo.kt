package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataserviceclient.api.StationLogo

class SampleStationLogo {
    companion object {

        const val URI_LOGO_1 = "content://de.esolutions.TestProvider/logo_1.png"
        const val URI_LOGO_SMALL_1 = "content://de.esolutions.TestProvider/logo_small_1.png"
        const val URI_LOGO_2 = "content://de.esolutions.TestProvider/logo_2.png"
        const val URI_LOGO_SMALL_2 = "content://de.esolutions.TestProvider/logo_small_2.png"

        val STATION_LOGO_WITH_INVALID_IMAGE_DATA
            get() = ImageEntity(
                uri = "uri",
                fileName = "file_name",
                imageType = ImageEntity.Companion.ImageType.LOGO,
                imageData = null,
                eTag = "logo_etag"
            )
        val IMAGE_ENTITY
            get() = ImageEntity(
                URI_LOGO_1,
                "file_name",
                ImageEntity.Companion.ImageType.LOGO,
                null,
                "logo_etag"
            )
        val STATION_LOGO_1 = StationLogo(
            URI_LOGO_1
        )
        val IMAGE_ENTITY_1
            get() = ImageEntity(
                URI_LOGO_1,
                "file_name",
                ImageEntity.Companion.ImageType.LOGO,
                listOf(),
                "logo_etag"
            )

        val STATION_LOGO_2 = StationLogo(
            URI_LOGO_2
        )
        val IMAGE_ENTITY_2
            get() = ImageEntity(
                URI_LOGO_2,
                "file_name_2",
                ImageEntity.Companion.ImageType.LOGO,
                listOf(),
                "logo_etag"
            )

        val STATION_LOGO_SMALL_1 = StationLogo(
            URI_LOGO_SMALL_1
        )
        val IMAGE_ENTITY_SMALL_1
            get() = ImageEntity(
                URI_LOGO_SMALL_1,
                "file_name",
                ImageEntity.Companion.ImageType.LOGO_SMALL,
                listOf(),
                "logo_small_etag"
            )

        val STATION_LOGO_SMALL_2 = StationLogo(
            URI_LOGO_SMALL_2
        )
        val IMAGE_ENTITY_SMALL_2
            get() = ImageEntity(
                URI_LOGO_SMALL_2,
                "file_name_2",
                ImageEntity.Companion.ImageType.LOGO_SMALL,
                listOf(),
                "logo_small_etag"
            )
    }
}

