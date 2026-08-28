package technology.cariad.entertainment.metadataservice.data.database.entity

import technology.cariad.entertainment.metadataservice.sampledata.SampleArtist.Companion.ARTIST_ENTRY_1
import org.junit.Assert
import org.junit.Test

class ArtistEntryTest {

    @Test
    fun artistEntryConstructor_shouldCreateEntry() {

        Assert.assertEquals(0, ARTIST_ENTRY_1.artistId)
        Assert.assertEquals("Madonna", ARTIST_ENTRY_1.artistName)
    }
}
