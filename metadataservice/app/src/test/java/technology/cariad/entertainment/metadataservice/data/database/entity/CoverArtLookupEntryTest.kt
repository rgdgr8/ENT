package technology.cariad.entertainment.metadataservice.data.database.entity

import technology.cariad.entertainment.metadataservice.sampledata.SampleCoverArtLookup.Companion.COVER_ART_LOOKUP_ENTRY_2
import org.junit.Assert
import org.junit.Test

class CoverArtLookupEntryTest {

    @Test
    fun coverArtLookupEntryConstructor_shouldCreateEntry() {

        Assert.assertEquals(1L, COVER_ART_LOOKUP_ENTRY_2.artistId)
        Assert.assertEquals(2L, COVER_ART_LOOKUP_ENTRY_2.albumId)
        Assert.assertEquals(3L, COVER_ART_LOOKUP_ENTRY_2.imageId)
        Assert.assertEquals("Cherish", COVER_ART_LOOKUP_ENTRY_2.trackName)
        Assert.assertEquals(4L, COVER_ART_LOOKUP_ENTRY_2.coverArtLookupId)
        Assert.assertEquals(10, COVER_ART_LOOKUP_ENTRY_2.lifeCycleDataId)
    }

    @Test
    fun coverArtLookupEntryConstructor_shouldCreateEntryWithoutOptionalParams() {

        val coverArtLookupEntry = CoverArtLookupEntry(
            albumId = 1L,
            coverArtLookupId = 2L,
            artistId = 3L,
            imageId = null,
            trackName = null,
            lifeCycleDataId = 10
        )

        Assert.assertNotEquals(null, coverArtLookupEntry)
        Assert.assertEquals(1L, coverArtLookupEntry.albumId)
        Assert.assertEquals(2L, coverArtLookupEntry.coverArtLookupId)
        Assert.assertEquals(3L, coverArtLookupEntry.artistId)
        Assert.assertEquals(10, coverArtLookupEntry.lifeCycleDataId)
    }
}
