package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataserviceclient.api.Station
import technology.cariad.entertainment.metadataservice.sampledata.SampleStation
import org.junit.Assert
import org.junit.Test

class StationMapperTest {

    @Test
    fun mapValue_shouldConvertStationEntityToStation() {
        val station = StationMapper().mapValue(SampleStation.STATION_ENTITY_1)

        Assert.assertEquals(SampleStation.STATION_1, station)
    }

    @Test
    fun mapValue_shouldConvertStationEntityWithNullsToStationWithNulls() {
        val station = StationMapper().mapValue(SampleStation.STATION_NULL_ENTITY_1)

        Assert.assertEquals(SampleStation.STATION_NULL_1, station)
    }

    @Test
    fun mapList_shouldConvertStationEntitiesToStations() {
        val stationEntities = listOf(SampleStation.STATION_ENTITY_1, SampleStation.STATION_ENTITY_1)

        val stations = StationMapper().mapList(stationEntities)

        val expectedStations = listOf(SampleStation.STATION_1, SampleStation.STATION_1)

        Assert.assertEquals(expectedStations, stations)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val emptyList = StationMapper().mapList(listOf())

        Assert.assertEquals(listOf<Station>(), emptyList)
    }
}

