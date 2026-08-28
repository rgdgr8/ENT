package technology.cariad.entertainment.metadataservice.common.codingprovider.data

/**
 * Coding values for the:
 * usb, port, brand
 *
 * Should be expanded with every coding that can be provided
 */
internal object MediaCodingValues {
    var countryOfSales: CountryOfSales? = null

    // For all the codings that the user ask for and are not yet implemented
    var extraCoding: HashMap<Int, ShortArray> = hashMapOf(0 to shortArrayOf(0))
}
