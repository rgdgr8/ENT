package technology.cariad.entertainment.metadataservice.data.database.converter

import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity.Companion.OriginType
import org.junit.Assert
import org.junit.Test

class OriginTypeConverterTest {

    private val originTypeConverter = OriginTypeConverter()

    @Test
    fun should_convertOriginTypeToInt() {
        val radiodns = originTypeConverter.fromOriginType(OriginType.RADIODNS)
        val radioplayer = originTypeConverter.fromOriginType(OriginType.RADIOPLAYER)
        val gracenote = originTypeConverter.fromOriginType(OriginType.GRACENOTE)
        val unknown = originTypeConverter.fromOriginType(OriginType.UNKNOWN)

        Assert.assertEquals(1, radiodns)
        Assert.assertEquals(2, radioplayer)
        Assert.assertEquals(3, gracenote)
        Assert.assertEquals(-1, unknown)
    }

    @Test
    fun should_convertIntInRangeToOriginType() {
        val radiodns = originTypeConverter.toOriginType(1)
        val radioplayer = originTypeConverter.toOriginType(2)
        val gracenote = originTypeConverter.toOriginType(3)

        Assert.assertEquals(OriginType.RADIODNS, radiodns)
        Assert.assertEquals(OriginType.RADIOPLAYER, radioplayer)
        Assert.assertEquals(OriginType.GRACENOTE, gracenote)
    }

    @Test
    fun should_convertIntOutOfRangeToUnknownOriginType() {
        val unknownResult_5 = originTypeConverter.toOriginType(-5)
        val unknownResult_1 = originTypeConverter.toOriginType(-1)
        val unknownResult0 = originTypeConverter.toOriginType(0)
        val unknownResult3 = originTypeConverter.toOriginType(4)
        val unknownResult17 = originTypeConverter.toOriginType(17)

        Assert.assertEquals(OriginType.UNKNOWN, unknownResult_5)
        Assert.assertEquals(OriginType.UNKNOWN, unknownResult_1)
        Assert.assertEquals(OriginType.UNKNOWN, unknownResult0)
        Assert.assertEquals(OriginType.UNKNOWN, unknownResult3)
        Assert.assertEquals(OriginType.UNKNOWN, unknownResult17)
    }
}

