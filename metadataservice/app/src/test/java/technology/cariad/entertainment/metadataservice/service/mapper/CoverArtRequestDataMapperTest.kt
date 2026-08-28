package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleCoverArtRequestData
import org.junit.Assert
import org.junit.Test

class CoverArtRequestDataMapperTest {

    @Test
    fun mapValue_shouldMapCoverArtRequestDataToCoverArtRequestDataEntity() {
        val coverArtRequestEntity =
            CoverArtRequestDataMapper().mapValue(SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_1)

        Assert.assertEquals(
            SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_1,
            coverArtRequestEntity
        )
    }

    @Test
    fun mapList_shouldMapCoverArtRequestDataToCoverArtRequestDataEntities() {
        val coverArtRequestEntities =
            CoverArtRequestDataMapper().mapList(
                listOf(
                    SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_1,
                    SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_1
                )
            )

        Assert.assertEquals(
            listOf(
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_1,
                SampleCoverArtRequestData.COVER_ART_REQUEST_DATA_ENTITY_1
            ), coverArtRequestEntities
        )
    }

    @Test
    fun mapList_shouldMapEmptyListToEmptyList() {
        val emptyList = CoverArtRequestDataMapper().mapList(listOf())

        Assert.assertEquals(listOf<CoverArtRequestDataEntity>(), emptyList)
    }
}

