package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.WebStream
import de.esolutions.connectedradioclient.data.enums.StreamQuality
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity
import org.junit.Assert
import org.junit.Test

class StationStreamMapperTest {

    private val stationStreamMapper = StationStreamMapper()

    private val webStream = WebStream(
        "https://www.teststream.com/stream.mp3/",
        5000,
        StreamQuality.HIGH
    )
    private val expectedStationStreamEntity =
        StationStreamEntity(
            "https://www.teststream.com/stream.mp3/",
            5000,
            StationStreamEntity.Companion.QualityType.HIGH
        )

    @Test
    fun mapValue_shouldConvertWebStreamToStationStreamEntity() {
        val stationStreamEntity = stationStreamMapper.mapValue(webStream)

        Assert.assertEquals(expectedStationStreamEntity, stationStreamEntity)
    }

    @Test
    fun mapList_shouldConvertWebStreamsToStationStreamEntities() {
        val stationStreamEntity = stationStreamMapper.mapList(listOf(webStream))

        Assert.assertEquals(listOf(expectedStationStreamEntity), stationStreamEntity)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val empty = stationStreamMapper.mapList(listOf())

        Assert.assertEquals(listOf<StationStreamEntity>(), empty)
    }
}

