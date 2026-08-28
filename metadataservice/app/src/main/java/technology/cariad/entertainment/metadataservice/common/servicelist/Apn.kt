package technology.cariad.entertainment.metadataservice.common.servicelist

import de.esolutions.coreservices.csm.service.systemservice.Apn as ApnExt

/**
 * Access point names
 * @see [de.esolutions.coreservices.csm.service.systemservice.Apn]
 */
enum class Apn {
    APN_NOT_ALLOWED,
    APN_VENDOR,
    APN_CUSTOMER,
    APN_DLINK
}

internal fun mapServiceListApn(apnExt: Int): Apn =
    when (apnExt) {
        ApnExt.APN_VENDOR -> Apn.APN_VENDOR
        ApnExt.APN_CUSTOMER, ApnExt.APN_ESIMONLY, ApnExt.APN_NOESIM -> Apn.APN_CUSTOMER
        ApnExt.APN_DLINK -> Apn.APN_DLINK
        ApnExt.APN_NOTALLOWED -> Apn.APN_NOT_ALLOWED
        else -> Apn.APN_NOT_ALLOWED
    }

