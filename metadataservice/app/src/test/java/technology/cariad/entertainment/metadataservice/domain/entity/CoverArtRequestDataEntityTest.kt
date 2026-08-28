package technology.cariad.entertainment.metadataservice.domain.entity

import org.junit.Assert
import org.junit.Test

class CoverArtRequestDataEntityTest {

    @Test
    fun coverArtRequestDataEntityConstructor_shouldCreateEntryWithoutOptionalParams() {

        val coverArtRequestDataEntity = CoverArtRequestDataEntity()

        Assert.assertEquals(null, coverArtRequestDataEntity.lifeCycleData)
        Assert.assertEquals(null, coverArtRequestDataEntity.artist)
        Assert.assertEquals(null, coverArtRequestDataEntity.album)
        Assert.assertEquals(null, coverArtRequestDataEntity.title)
    }
}
