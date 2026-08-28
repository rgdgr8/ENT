package technology.cariad.entertainment.metadataservice.data.database.entity

import technology.cariad.entertainment.metadataservice.sampledata.SampleImage.Companion.IMAGE_ENTRY_1
import org.junit.Assert
import org.junit.Test

class ImageEntryTest {

    @Test
    fun imageEntryConstructor_shouldCreateEntry() {

        Assert.assertEquals(0L, IMAGE_ENTRY_1.imageId)
        Assert.assertEquals("fileName.jpg", IMAGE_ENTRY_1.fileName)
        Assert.assertEquals(3, IMAGE_ENTRY_1.imageType)
    }
}
