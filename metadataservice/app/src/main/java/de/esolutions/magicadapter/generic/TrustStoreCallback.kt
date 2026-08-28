package de.esolutions.magicadapter.generic

import javax.net.ssl.TrustManager

/**
 * LOCAL BUILD STUB: Replaces de.esolutions.magicadapter.generic.TrustStoreCallback
 */
interface TrustStoreCallback {
    fun trustStoreByNameResponse(trustStoreId: String, trustStoreAsPemString: String?)
    fun trustStoreManagerResponse(operationId: String, trustManager: TrustManager?)
}
