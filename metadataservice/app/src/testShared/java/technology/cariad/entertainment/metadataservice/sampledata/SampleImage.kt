package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.data.database.entity.ImageEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationImageEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity

class SampleImage {
    companion object {
        val IMAGE_ENTRY_1 = ImageEntry(
            imageId = 0L,
            fileName = "fileName.jpg",
            imageType = 3
        )

        val IMAGE_ENTRY_2 = ImageEntry(
            imageId = 0L,
            fileName = "fileName.jpg",
            imageType = 3
        )
        val IMAGE_ENTRY_3 = ImageEntry(
            imageId = 1L,
            fileName = "logo01.jpg",
            imageType = 2
        )
        val IMAGE_ENTRY_4 = ImageEntry(
            imageId = 2L,
            fileName = "logo02.jpg",
            imageType = 2
        )

        val IMAGE_ENTITY_1 = ImageEntity(
            imageData = null,
            uri = null,
            fileName = "fileName.png",
            imageType = ImageEntity.Companion.ImageType.COVER_ART
        )

        val IMAGE_ENTITY_2 = ImageEntity(
            imageData = null,
            uri = "uri",
            fileName = "fileName.png",
            imageType = ImageEntity.Companion.ImageType.COVER_ART
        )

        val IMAGE_ENTITY_WITH_IMAGE_DATA = ImageEntity(
            imageData = listOf(122, 98, 98),
            uri = "uri",
            fileName = "fileName.png",
            imageType = ImageEntity.Companion.ImageType.COVER_ART
        )

        val IMAGE_ENTITY_WITHOUT_MIME_TYPE = ImageEntity(
            imageData = null,
            uri = null,
            fileName = "fileName.png",
            imageType = ImageEntity.Companion.ImageType.COVER_ART
        )

        val STATION_IMAGE_ENTRY_1 = StationImageEntry(
            imageId = 1L,
            stationId = 2L
        )
    }
}
