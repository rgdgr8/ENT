package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import technology.cariad.entertainment.metadataserviceclient.util.DabBearer
import technology.cariad.entertainment.metadataserviceclient.util.FmRdsBearer
import technology.cariad.entertainment.metadataserviceclient.util.IbocBearer
import org.junit.Assert
import org.junit.Test

class BearerMapperTest {

    @Test
    fun mapValue_shouldMapFmRdsBearerToFmBearerEntity() {
        val fmBearer = SampleBearer.FM_BEARER_ENTITY_1.let {
            FmRdsBearer(it.pi, it.frequencyKhz, it.ecc, it.mcc)
        }
        val fmBearerEntity = BearerMapper().mapValue(fmBearer)

        Assert.assertEquals(SampleBearer.FM_BEARER_ENTITY_1, fmBearerEntity)
    }

    @Test
    fun mapValue_shouldMapIbocBearerToIbocBearerEntity() {
        val ibocBearer = SampleBearer.IBOC_BEARER_ENTITY_1.let {
            IbocBearer(it.tx, it.cc, it.multicastId)
        }
        val ibocBearerEntity = BearerMapper().mapValue(ibocBearer)

        Assert.assertEquals(SampleBearer.IBOC_BEARER_ENTITY_1, ibocBearerEntity)
    }

    @Test
    fun mapValue_shouldMapDabBearerToDabBearerEntity() {
        val dabBearer = SampleBearer.DAB_BEARER_ENTITY_1.let {
            DabBearer(it.sId, it.eId, it.scIds, it.ecc, it.mcc)
        }
        val dabBearerEntity = BearerMapper().mapValue(dabBearer)

        Assert.assertEquals(SampleBearer.DAB_BEARER_ENTITY_1, dabBearerEntity)
    }

    @Test
    fun mapList_shouldMapBearersToBearerEntities() {
        val fmBearer = SampleBearer.FM_BEARER_ENTITY_1.let {
            FmRdsBearer(it.pi, it.frequencyKhz, it.ecc, it.mcc)
        }

        val ibocBearer = SampleBearer.IBOC_BEARER_ENTITY_1.let {
            IbocBearer(it.tx, it.cc, it.multicastId)
        }

        val dabBearer = SampleBearer.DAB_BEARER_ENTITY_1.let {
            DabBearer(it.sId, it.eId, it.scIds, it.ecc, it.mcc)
        }

        val bearerEntities = BearerMapper().mapList(listOf(fmBearer, ibocBearer, dabBearer))

        Assert.assertEquals(
            listOf(
                SampleBearer.FM_BEARER_ENTITY_1,
                SampleBearer.IBOC_BEARER_ENTITY_1,
                SampleBearer.DAB_BEARER_ENTITY_1
            ), bearerEntities
        )
    }

    @Test
    fun mapList_shouldMapEmptyListToEmptyList() {
        val list = BearerMapper().mapList(listOf())

        Assert.assertEquals(listOf<IBearerEntity>(), list)
    }

    @Test
    fun mapValue_shouldMapFmBearerEntityToStationBearer() {
        val stationBearer = BearerEntityMapper().mapValue(SampleBearer.FM_BEARER_ENTITY_1)

        val expectedStationBearer = SampleBearer.FM_BEARER_ENTITY_1.let {
            FmRdsBearer(it.pi, it.frequencyKhz, it.ecc, it.mcc).rawBearer
        }

        Assert.assertEquals(expectedStationBearer, stationBearer)
    }

    @Test
    fun mapValue_shouldMapIbocBearerEntityToStationBearer() {
        val stationBearer = BearerEntityMapper().mapValue(SampleBearer.IBOC_BEARER_ENTITY_1)

        val expectedStationBearer = SampleBearer.IBOC_BEARER_ENTITY_1.let {
            IbocBearer(it.tx, it.cc, it.multicastId).rawBearer
        }

        Assert.assertEquals(expectedStationBearer, stationBearer)
    }

    @Test
    fun mapValue_shouldMapDabBearerEntityToStationBearer() {
        val stationBearer = BearerEntityMapper().mapValue(SampleBearer.DAB_BEARER_ENTITY_1)

        val expectedStationBearer = SampleBearer.DAB_BEARER_ENTITY_1.let {
            DabBearer(it.sId, it.eId, it.scIds, it.ecc, it.mcc).rawBearer
        }

        Assert.assertEquals(expectedStationBearer, stationBearer)
    }

    @Test
    fun mapList_shouldMapBearerEntitiesToStationBearers() {
        val bearers = BearerEntityMapper().mapList(
            listOf(
                SampleBearer.FM_BEARER_ENTITY_1,
                SampleBearer.IBOC_BEARER_ENTITY_1,
                SampleBearer.DAB_BEARER_ENTITY_1
            )
        )

        val expectedBearers = listOf(
            SampleBearer.FM_BEARER_ENTITY_1.let {
                FmRdsBearer(it.pi, it.frequencyKhz, it.ecc, it.mcc).rawBearer
            }
            , SampleBearer.IBOC_BEARER_ENTITY_1.let {
                IbocBearer(it.tx, it.cc, it.multicastId).rawBearer
            }
            , SampleBearer.DAB_BEARER_ENTITY_1.let {
                DabBearer(it.sId, it.eId, it.scIds, it.ecc, it.mcc).rawBearer
            }
        )

        Assert.assertEquals(expectedBearers, bearers)
    }

    @Test
    fun mapList_shouldMapEmptyEntityListToEmptyList() {
        val list = BearerEntityMapper().mapList(listOf())

        Assert.assertEquals(listOf<IBearerEntity>(), list)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mapValue_shouldThrowIfCalledWithUnknownBearerEntityType() {
        val amBearer = object : IBearerEntity {}
        BearerEntityMapper().mapValue(amBearer)
    }
}

