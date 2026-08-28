package technology.cariad.entertainment.metadataservice.common.util

import technology.cariad.entertainment.metadataservice.domain.entity.DabBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IbocBearerEntity
import org.junit.Assert
import org.junit.Test

private val FAKE_FM_BEARER =
    FmRdsBearerEntity(pi = "c586", frequencyKhz = "09580", mcc = "261", ecc = "e1")
private val FAKE_FM_BEARER_2 =
    FmRdsBearerEntity(pi = "c586", frequencyKhz = "09580", mcc = "261", ecc = null)

private val FAKE_DAB_BEARER =
    DabBearerEntity(eId = "100c", sId = "d220", scIds = "0", mcc = "261", ecc = "e0")

private val FAKE_HD_BEARER = IbocBearerEntity(tx = "07426", cc = "292", multicastId = "2")

private val FAKE_FM_BEARER_URI = "fm:c586.09580.261.e1"
private val FAKE_FM_BEARER_URI_2 = "fm:c586.09580.261"

private val FAKE_DAB_BEARER_URI = "dab:100c.d220.0.261.e0"

private val FAKE_HD_BEARER_URI = "hd:07426.292.2"
private val FAKE_HD_BEARER_URI_2 = "hd:07426.292"

class BearerStringUtilsTest {

    @Test
    fun toBearerString_shouldConvertToFmString() {
        Assert.assertEquals(FAKE_FM_BEARER_URI, FAKE_FM_BEARER.toBearerString())
    }

    @Test
    fun toBearerString_shouldConvertToDabString() {
        Assert.assertEquals(FAKE_DAB_BEARER_URI, FAKE_DAB_BEARER.toBearerString())
    }

    @Test
    fun toBearerString_shouldConvertToHdString() {
        Assert.assertEquals(FAKE_HD_BEARER_URI, FAKE_HD_BEARER.toBearerString())
    }

    @Test
    fun fromBearerString_shouldConvertToFmBearer() {
        Assert.assertEquals(FAKE_FM_BEARER, fromBearerString(FAKE_FM_BEARER_URI))
    }

    @Test
    fun fromBearerString_shouldConvertToDabBearer() {
        Assert.assertEquals(FAKE_DAB_BEARER, fromBearerString(FAKE_DAB_BEARER_URI))
    }

    @Test
    fun fromBearerString_shouldConvertToHdBearer() {
        Assert.assertEquals(FAKE_HD_BEARER, fromBearerString(FAKE_HD_BEARER_URI))
    }

    @Test
    fun fromBearerString_shouldConvertToHdBearerAndSetMulticastIdToOneIfMissing() {
        val ibocBearer = fromBearerString(FAKE_HD_BEARER_URI_2) as IbocBearerEntity
        Assert.assertEquals("1", ibocBearer.multicastId)
    }

    @Test
    fun toBearerString_shouldConvertToFmStringMissingEcc() {
        Assert.assertEquals(FAKE_FM_BEARER_URI_2, FAKE_FM_BEARER_2.toBearerString())
    }

    @Test
    fun fromBearerString_shouldConvertToFmBearerMissingEcc() {
        Assert.assertEquals(FAKE_FM_BEARER_2, fromBearerString(FAKE_FM_BEARER_URI_2))
    }
}

