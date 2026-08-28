package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataserviceclient.api.StationStream
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationStream
import org.junit.Assert
import org.junit.Test

class StationStreamMapperTest {

    @Test
    fun mapValue_shouldConvertStationStreamEntityToStationStream() {
        val stationStream =
            StationStreamMapper().mapValue(SampleStationStream.STATION_STREAM_ENTITY_1)

        Assert.assertEquals(SampleStationStream.STATION_STREAM_1, stationStream)
    }

    @Test
    fun mapList_shouldConvertStationStreamEntitiesToStationStreams() {
        val stationStreamEntities = listOf(
            SampleStationStream.STATION_STREAM_ENTITY_1,
            SampleStationStream.STATION_STREAM_ENTITY_1
        )

        val stationStreams = StationStreamMapper().mapList(stationStreamEntities)

        val expectedStationStreams =
            listOf(SampleStationStream.STATION_STREAM_1, SampleStationStream.STATION_STREAM_1)

        Assert.assertEquals(expectedStationStreams, stationStreams)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val emptyList = StationStreamMapper().mapList(listOf())

        Assert.assertEquals(listOf<StationStream>(), emptyList)
    }
}

