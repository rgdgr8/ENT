package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.data.database.entity.StreamEntry
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

private const val STREAM_URL = "https://samplestream.com/stream.mp3/"

class StreamDaoTest {

    private var streamDao: StreamDao = mockk()

    @Test
    fun createStreamEntry_shouldConvertEntityToEntry() {

        every { streamDao.createStreamEntry(any(), any(), any()) } answers { callOriginal() }

        val streamEntry = streamDao.createStreamEntry(
            StationStreamEntity(
                STREAM_URL, 200, StationStreamEntity.Companion.QualityType.HIGH
            ), 1, 2
        )

        val expectedStreamEntry =
            StreamEntry(0, 1, 2, STREAM_URL, 200, StationStreamEntity.Companion.QualityType.HIGH)

        Assert.assertEquals(expectedStreamEntry, streamEntry)
    }
}

