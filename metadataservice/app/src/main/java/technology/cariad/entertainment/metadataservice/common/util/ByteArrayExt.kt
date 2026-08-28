package technology.cariad.entertainment.metadataservice.common.util

/**
 * Extension function to convert a ByteArray into a hex string e.g. for logging purposes.
 */
fun ByteArray.contentToHexString() = joinToString("") { "%02x".format(it) }

