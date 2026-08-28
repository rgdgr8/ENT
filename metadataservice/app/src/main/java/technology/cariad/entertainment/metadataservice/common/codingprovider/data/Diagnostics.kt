package technology.cariad.entertainment.metadataservice.common.codingprovider.data

/**
 * Model of constant data needed to get the coding value
 * @param blobID specific ID of the blob 0x600, 0x981...
 * @param byteWithValue byte with the info of the coding that must be analyzed
 * @param startBit
 * @param length with the startBit select the important bits of the byte with the value
 */
internal data class Diagnostics(
    val blobID: Int,
    val byteWithValue: Int,
    val startBit: Int,
    val length: Int,
)
