package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.bearer.DabBearer
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.data.bearer.IBearer
import technology.cariad.entertainment.metadataservice.domain.entity.DabBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IbocBearerEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import org.junit.Assert
import org.junit.Test

class BearerMapperTest {

    private val bearerMapper = BearerMapper()
    private val bearerEntityMapper = BearerEntityMapper()

    @Test
    fun mapValue_shouldConvertValidFmBearerEntityToFmBearer() {

        val bearer = bearerMapper.mapValue(SampleBearer.FM_BEARER_ENTITY_1)

        Assert.assertTrue(bearer is FmBearer)

        val fmBearer = bearer as FmBearer

        Assert.assertTrue(fmBearer.frequency == SampleBearer.FM_BEARER_ENTITY_1.frequencyKhz)
        Assert.assertTrue(fmBearer.mcc == SampleBearer.FM_BEARER_ENTITY_1.mcc)
        Assert.assertTrue(fmBearer.pi == SampleBearer.FM_BEARER_ENTITY_1.pi)
    }

    @Test
    fun mapValue_shouldConvertValidFmBearerToFmBearerEntity() {

        val fmBearer = SampleBearer.FM_BEARER_ENTITY_1.let {
            FmBearer(it.mcc, it.pi, it.frequencyKhz)
        }

        val bearerEntity = bearerEntityMapper.mapValue(fmBearer)

        Assert.assertTrue(bearerEntity is FmRdsBearerEntity)

        val fmBearerEntity = bearerEntity as FmRdsBearerEntity

        Assert.assertEquals(SampleBearer.FM_BEARER_ENTITY_1, fmBearerEntity)
    }

    @Test
    fun mapValue_shouldConvertValidIbocBearerEntityToHdBearer() {

        val bearer = bearerMapper.mapValue(SampleBearer.IBOC_BEARER_ENTITY_1)

        Assert.assertTrue(bearer is HdBearer)

        val hdBearer = bearer as HdBearer

        Assert.assertTrue(hdBearer.cc == SampleBearer.IBOC_BEARER_ENTITY_1.cc)
        Assert.assertTrue(hdBearer.multicastId == SampleBearer.IBOC_BEARER_ENTITY_1.multicastId)
        Assert.assertTrue(hdBearer.tx == SampleBearer.IBOC_BEARER_ENTITY_1.tx)
    }

    @Test
    fun mapValue_shouldConvertValidHdBearerToIbocBearerEntity() {

        val hdBearer = SampleBearer.IBOC_BEARER_ENTITY_1.let {
            HdBearer(it.tx, it.cc, it.multicastId!!)
        }
        val bearerEntity = bearerEntityMapper.mapValue(hdBearer)

        Assert.assertTrue(bearerEntity is IbocBearerEntity)

        val ibocBearerEntity = bearerEntity as IbocBearerEntity

        Assert.assertEquals(SampleBearer.IBOC_BEARER_ENTITY_1, ibocBearerEntity)
    }

    @Test
    fun mapValue_shouldConvertValidDabBearerEntityToDabBearer() {

        val bearer = bearerMapper.mapValue(SampleBearer.DAB_BEARER_ENTITY_1)

        Assert.assertTrue(bearer is DabBearer)

        val dabBearer = bearer as DabBearer

        Assert.assertTrue(dabBearer.eid == SampleBearer.DAB_BEARER_ENTITY_1.eId)
        Assert.assertTrue(dabBearer.sid == SampleBearer.DAB_BEARER_ENTITY_1.sId)
        Assert.assertTrue(dabBearer.scids == SampleBearer.DAB_BEARER_ENTITY_1.scIds)
        Assert.assertTrue(dabBearer.mcc == SampleBearer.DAB_BEARER_ENTITY_1.mcc)
        Assert.assertTrue(dabBearer.ecc == SampleBearer.DAB_BEARER_ENTITY_1.ecc)
    }

    @Test
    fun mapValue_shouldConvertValidDabBearerToDabBearerEntity() {

        val dabBearer = SampleBearer.DAB_BEARER_ENTITY_1.let {
            DabBearer(it.mcc, it.ecc, it.eId, it.sId, it.scIds)
        }
        val bearerEntity = bearerEntityMapper.mapValue(dabBearer)

        Assert.assertTrue(bearerEntity is DabBearerEntity)

        val dabBearerEntity = bearerEntity as DabBearerEntity

        Assert.assertEquals(SampleBearer.DAB_BEARER_ENTITY_1, dabBearerEntity)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mapValue_shouldThrowIfCallWithUnknownBearerEntityType() {
        val amBearer = object : IBearerEntity {}
        bearerMapper.mapValue(amBearer)
    }

    @Test(expected = IllegalArgumentException::class)
    fun mapValue_shouldThrowIfCallWithUnknownBearerType() {
        val amBearer = object : IBearer {
            override fun getBearerString() = ""
        }
        bearerEntityMapper.mapValue(amBearer)
    }
}

