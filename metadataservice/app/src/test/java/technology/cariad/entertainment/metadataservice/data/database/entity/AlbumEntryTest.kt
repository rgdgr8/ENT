package technology.cariad.entertainment.metadataservice.data.database.entity

import technology.cariad.entertainment.metadataservice.sampledata.SampleAlbum.Companion.ALBUM_ENTRY_1
import org.junit.Assert
import org.junit.Test

class AlbumEntryTest {

    @Test
    fun albumEntryConstructor_shouldCreateEntry() {

        Assert.assertEquals(0, ALBUM_ENTRY_1.albumId)
        Assert.assertEquals("Like a Prayer", ALBUM_ENTRY_1.albumName)
    }
}
