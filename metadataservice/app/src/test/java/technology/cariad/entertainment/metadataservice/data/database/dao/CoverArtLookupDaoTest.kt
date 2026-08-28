package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.LifeCycleDataEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleCoverArtLookup.Companion.COVER_ART_LOOKUP_ENTRY_1
import technology.cariad.entertainment.metadataservice.sampledata.SampleLifeCycleData
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class CoverArtLookupDaoTest {

    private val coverArtLookupDao: CoverArtLookupDao = mockk(relaxed = true)

    @Test
    fun createCoverArtLookupEntry_shouldCreateEntryByParams() {
        val coverArtRequestData = CoverArtRequestDataEntity(
            album = "Like a Prayer",
            title = "Love Song",
            artist = "Madonna",
            lifeCycleData = null
        )

        every { coverArtLookupDao.createCoverArtLookupEntry(any(), any(), any(), any(), any()) } answers { callOriginal() }

        val coverArtLookupEntry = coverArtLookupDao.createCoverArtLookupEntry(
            albumId = 1,
            artistId = 8,
            imageId = 10,
            coverArtRequestData = coverArtRequestData,
            lifeCycleDataId = 20
        )
        Assert.assertEquals(COVER_ART_LOOKUP_ENTRY_1, coverArtLookupEntry)
    }
}
