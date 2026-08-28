package technology.cariad.entertainment.metadataservice.common.servicelist

import org.junit.Assert
import org.junit.Test
import de.esolutions.coreservices.csm.service.systemservice.Apn as ApnExt

class ApnTest {

    @Test
    fun mapServiceListApn_shouldMapValidApns() {
        Assert.assertEquals(mapServiceListApn(ApnExt.APN_VENDOR), Apn.APN_VENDOR)

        Assert.assertEquals(mapServiceListApn(ApnExt.APN_CUSTOMER), Apn.APN_CUSTOMER)
        Assert.assertEquals(mapServiceListApn(ApnExt.APN_NOESIM), Apn.APN_CUSTOMER)
        Assert.assertEquals(mapServiceListApn(ApnExt.APN_ESIMONLY), Apn.APN_CUSTOMER)
    }

    @Test
    fun mapServiceListApn_shouldMapNotAllowed() {
        Assert.assertEquals(mapServiceListApn(ApnExt.APN_NOTALLOWED), Apn.APN_NOT_ALLOWED)
    }

    @Test
    fun mapServiceListApn_shouldReturnNotAllowedForInvalidApn() {
        Assert.assertEquals(mapServiceListApn(-1), Apn.APN_NOT_ALLOWED)
        Assert.assertEquals(mapServiceListApn(5), Apn.APN_NOT_ALLOWED)
    }
}

