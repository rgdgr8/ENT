package technology.cariad.entertainment.metadataservice.data.database.converter

import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import org.junit.Assert
import org.junit.Test

class QualityTypeConverterTest {

    private val qualityTypeConverter = QualityTypeConverter()

    @Test
    fun should_convertQualityTypeToInt() {
        val highQuality = qualityTypeConverter.fromQualityType(StationStreamEntity.Companion.QualityType.HIGH)
        val lowQuality = qualityTypeConverter.fromQualityType(StationStreamEntity.Companion.QualityType.LOW)
        val unknown = qualityTypeConverter.fromQualityType(StationStreamEntity.Companion.QualityType.UNKNOWN)

        Assert.assertEquals(1, highQuality)
        Assert.assertEquals(2, lowQuality)
        Assert.assertEquals(-1, unknown)
    }

    @Test
    fun should_convertIntInRangeToQualityType() {
        val highQuality = qualityTypeConverter.toQualityType(1)
        val lowQuality = qualityTypeConverter.toQualityType(2)

        Assert.assertEquals(StationStreamEntity.Companion.QualityType.HIGH, highQuality)
        Assert.assertEquals(StationStreamEntity.Companion.QualityType.LOW, lowQuality)
    }

    @Test
    fun should_convertIntOutOfRangeToUnknownQualityType() {
        val unknownResult_5 = qualityTypeConverter.toQualityType(-5)
        val unknownResult_1 = qualityTypeConverter.toQualityType(-1)
        val unknownResult0 = qualityTypeConverter.toQualityType(0)
        val unknownResult3 = qualityTypeConverter.toQualityType(4)
        val unknownResult17 = qualityTypeConverter.toQualityType(17)

        Assert.assertEquals(StationStreamEntity.Companion.QualityType.UNKNOWN, unknownResult_5)
        Assert.assertEquals(StationStreamEntity.Companion.QualityType.UNKNOWN, unknownResult_1)
        Assert.assertEquals(StationStreamEntity.Companion.QualityType.UNKNOWN, unknownResult0)
        Assert.assertEquals(StationStreamEntity.Companion.QualityType.UNKNOWN, unknownResult3)
        Assert.assertEquals(StationStreamEntity.Companion.QualityType.UNKNOWN, unknownResult17)
    }
}
