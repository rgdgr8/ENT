package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataserviceclient.api.StationMetadata
import technology.cariad.entertainment.metadataservice.sampledata.SampleStationMetadata
import org.junit.Assert
import org.junit.Test

class StationMetadataMapperTest {

    @Test
    fun mapValue_shouldConvertStationMetadataEntityToStationMetadata() {
        val stationMetadata =
            StationMetadataMapper().mapValue(SampleStationMetadata.STATION_METADATA_ENTITY_1)

        Assert.assertEquals(SampleStationMetadata.STATION_METADATA_1, stationMetadata)
    }

    @Test
    fun mapValue_shouldConvertStationMetadataEntityWithoutGenreToStationMetadataWithoutGenre() {
        val stationMetadata =
            StationMetadataMapper().mapValue(SampleStationMetadata.STATION_METADATA_ENTITIY_WITHOUT_GENRE_1)

        Assert.assertEquals(SampleStationMetadata.STATION_METADATA_WITHOUT_GENRE_1, stationMetadata)
    }

    @Test
    fun mapList_shouldConvertStationMetadataEntitiesToStationMetadata() {
        val stationMetadataEntities = listOf(
            SampleStationMetadata.STATION_METADATA_ENTITY_1,
            SampleStationMetadata.STATION_METADATA_ENTITY_1
        )

        val stationMetadataList = StationMetadataMapper().mapList(stationMetadataEntities)

        val expectedStationMetadataList = listOf(
            SampleStationMetadata.STATION_METADATA_1,
            SampleStationMetadata.STATION_METADATA_1
        )

        Assert.assertEquals(expectedStationMetadataList, stationMetadataList)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val emptyList = StationMetadataMapper().mapList(listOf())

        Assert.assertEquals(listOf<StationMetadata>(), emptyList)
    }
}

