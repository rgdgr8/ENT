package technology.cariad.entertainment.metadataservice.common.codingprovider.data

import technology.cariad.entertainment.metadataservice.stubs.fw.asi.Consts.diag_id_android

/**
 * All the constants needed for the lib
 * The internal ones must not be used by the user
 */

internal object CodingConst {
    const val ASI_STORAGE_INSTANCE_ID = diag_id_android.toInt()

    const val FUNCTIONCONFIGURATION_HMI_BLOB_ID = 0x8802

    val countryOfSalesFirstByte = Diagnostics(FUNCTIONCONFIGURATION_HMI_BLOB_ID, 0, 0, 8 )
    val countryOfSalesSecondByte = Diagnostics(FUNCTIONCONFIGURATION_HMI_BLOB_ID, 1, 0, 8 )

}
