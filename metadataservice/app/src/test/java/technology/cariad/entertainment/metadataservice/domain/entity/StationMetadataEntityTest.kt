package technology.cariad.entertainment.metadataservice.domain.entity

import org.junit.Assert
import org.junit.Test

class StationMetadataEntityTest {

    @Test
    fun stationMetadataEntityConstructor_shouldCreateEntryWithoutOptionalParams() {

        val stationMetadataEntity = StationMetadataEntity(genres = emptyList(), etag = "etag")

        Assert.assertEquals(null, stationMetadataEntity.description)
        Assert.assertEquals(emptyList<String>(), stationMetadataEntity.genres)
        Assert.assertEquals(null, stationMetadataEntity.longName)
        Assert.assertEquals(null, stationMetadataEntity.mediumName)
        Assert.assertEquals(null, stationMetadataEntity.shortName)
    }
}
