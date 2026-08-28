package technology.cariad.entertainment.metadataservice.common.codingprovider

import android.content.Context
import technology.cariad.entertainment.metadataservice.common.codingprovider.core.CodingManager
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.CountryOfSales
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.MediaCodingValues

class MediaCodingProvider(context: Context) {

    private val codingManager = CodingManager(context)

    /**
     * Start the connection to the library
     */
    fun start(listener: IMediaCodingProviderListener): Boolean =
        codingManager.start(listener)

    /**
     * Stop the connection to the library
     */
    fun stop() {
        codingManager.stop()
    }

    /**
     * Country of sales coding of the target
     */
    fun getCountryOfSales(): CountryOfSales? =
        MediaCodingValues.countryOfSales
}

