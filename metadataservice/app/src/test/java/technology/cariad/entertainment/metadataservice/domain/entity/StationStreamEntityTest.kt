package technology.cariad.entertainment.metadataservice.domain.entity

import org.junit.Assert
import org.junit.Test

class StationStreamEntityTest {

    @Test
    fun stationStreamEntityConstructor_shouldCreateEntryWithoutOptionalParams() {

        val stationStreamEntity = StationStreamEntity(
            url = "url",
            quality = StationStreamEntity.Companion.QualityType.UNKNOWN
        )

        Assert.assertEquals(StationStreamEntity.Companion.QualityType.UNKNOWN, stationStreamEntity.quality)
        Assert.assertEquals(null, stationStreamEntity.offsetMillis)
        Assert.assertEquals("url", stationStreamEntity.url)
    }
}
