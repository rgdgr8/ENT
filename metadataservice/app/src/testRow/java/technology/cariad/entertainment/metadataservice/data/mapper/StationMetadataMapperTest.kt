package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.StationMetadata
import technology.cariad.entertainment.metadataservice.domain.entity.StationMetadataEntity
import org.junit.Assert
import org.junit.Test

class StationMetadataMapperTest {

    private val stationMetadataMapper = StationMetadataMapper()

    private val stationMetadata = StationMetadata(
        listOf(),
        "short name",
        "medium name",
        "long name",
        "description",
        listOf("Rock", "80s Rock", "Alternative 80s Rock")
    )

    private val expectedStationMetadataEntity = StationMetadataEntity(
        "short name",
        "medium name",
        "long name",
        "description",
        listOf("Rock", "80s Rock", "Alternative 80s Rock"),
        null
    )

    @Test
    fun mapValue_shouldConvertStationMetadataToStationMetadataEntity() {
        val stationMetadataEntity = stationMetadataMapper.mapValue(stationMetadata)

        Assert.assertEquals(expectedStationMetadataEntity, stationMetadataEntity)
    }

    @Test
    fun mapList_shouldConvertStationMetadataToStationMetadataEntities() {
        val stationMetadataEntities = stationMetadataMapper.mapList(listOf(stationMetadata))

        Assert.assertEquals(listOf(expectedStationMetadataEntity), stationMetadataEntities)
    }

    @Test
    fun mapList_shouldConvertEmptyListToEmptyList() {
        val empty = stationMetadataMapper.mapList(listOf())

        Assert.assertEquals(listOf<StationMetadataEntity>(), empty)
    }
}

