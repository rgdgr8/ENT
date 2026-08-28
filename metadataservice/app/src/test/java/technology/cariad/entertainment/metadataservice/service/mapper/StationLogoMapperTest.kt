package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataserviceclient.api.StationLogo
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationLogo
import org.junit.Assert
import org.junit.Test

class StationLogoMapperTest {

    @Test
    fun mapValue_shouldConvertImageEntityToStationLogo() {
        val stationLogo = StationLogoMapper().mapValue(SampleStationLogo.IMAGE_ENTITY_1)

        Assert.assertEquals(SampleStationLogo.STATION_LOGO_1, stationLogo)
    }

    @Test
    fun mapList_shouldConvertImageEntitiesToStationLogos() {
        val imageEntities =
            listOf(SampleStationLogo.IMAGE_ENTITY_1, SampleStationLogo.IMAGE_ENTITY_1)

        val stationLogos = StationLogoMapper().mapList(imageEntities)

        val expectedStationLogos =
            listOf(SampleStationLogo.STATION_LOGO_1, SampleStationLogo.STATION_LOGO_1)

        Assert.assertEquals(expectedStationLogos, stationLogos)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val emptyList = StationLogoMapper().mapList(listOf())

        Assert.assertEquals(listOf<StationLogo>(), emptyList)
    }
}

