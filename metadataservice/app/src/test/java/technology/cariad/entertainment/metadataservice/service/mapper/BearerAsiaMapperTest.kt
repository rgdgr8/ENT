package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.sampledata.SampleBearerAsia
import technology.cariad.entertainment.metadataservice.service.mapper.BearerAsiaMapper
import org.junit.Assert
import org.junit.Test

class BearerAsiaMapperTest {

    @Test
    fun mapValue_shouldConvertToEntity() {
        val bearerEntity = BearerAsiaMapper().mapValue(SampleBearerAsia.BEARER_ASIA_FM_1)

        Assert.assertEquals(SampleBearerAsia.BEARER_ASIA_FM_ENTITY_1, bearerEntity)
    }

    @Test
    fun mapList_shouldConvertToEntities() {
        val bearerEntities = BearerAsiaMapper().mapList(
            listOf(
                SampleBearerAsia.BEARER_ASIA_FM_1,
                SampleBearerAsia.BEARER_ASIA_AM_1
            )
        )

        Assert.assertEquals(
            listOf(
                SampleBearerAsia.BEARER_ASIA_FM_ENTITY_1,
                SampleBearerAsia.BEARER_ASIA_AM_ENTITY_1
            ), bearerEntities
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun mapValue_shouldThrowForInvalidType() {
        BearerAsiaMapper().mapValue(SampleBearerAsia.BEARER_ASIA_INVALID_TYPE)
    }
}

