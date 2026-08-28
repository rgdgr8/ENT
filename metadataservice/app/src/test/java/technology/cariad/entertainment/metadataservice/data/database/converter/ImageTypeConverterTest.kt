package technology.cariad.entertainment.metadataservice.data.database.converter

import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity.Companion.ImageType
import org.junit.Assert
import org.junit.Test

class ImageTypeConverterTest {

    private val imageTypeConverter = ImageTypeConverter()

    @Test
    fun should_convertImageTypeToInt() {
        val logo = imageTypeConverter.fromLogoType(ImageType.LOGO)
        val logoSmall = imageTypeConverter.fromLogoType(ImageType.LOGO_SMALL)
        val unknown = imageTypeConverter.fromLogoType(ImageType.UNKNOWN)

        Assert.assertEquals(1, logo)
        Assert.assertEquals(2, logoSmall)
        Assert.assertEquals(-1, unknown)
    }

    @Test
    fun should_convertIntInRangeToImageType() {
        val logo = imageTypeConverter.toLogoType(1)
        val logoSmall = imageTypeConverter.toLogoType(2)

        Assert.assertEquals(ImageType.LOGO, logo)
        Assert.assertEquals(ImageType.LOGO_SMALL, logoSmall)
    }

    @Test
    fun should_convertIntOutOfRangeToUnknownImageType() {
        val unknownResult_5 = imageTypeConverter.toLogoType(-5)
        val unknownResult_1 = imageTypeConverter.toLogoType(-1)
        val unknownResult0 = imageTypeConverter.toLogoType(0)
        val unknownResult3 = imageTypeConverter.toLogoType(4)
        val unknownResult17 = imageTypeConverter.toLogoType(17)

        Assert.assertEquals(ImageType.UNKNOWN, unknownResult_5)
        Assert.assertEquals(ImageType.UNKNOWN, unknownResult_1)
        Assert.assertEquals(ImageType.UNKNOWN, unknownResult0)
        Assert.assertEquals(ImageType.UNKNOWN, unknownResult3)
        Assert.assertEquals(ImageType.UNKNOWN, unknownResult17)
    }
}

