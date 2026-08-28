package technology.cariad.entertainment.metadataservice.common.codingprovider.core

import technology.cariad.entertainment.metadataservice.common.codingprovider.IMediaCodingProviderListener
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.CodingConst
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.CountryOfSales
import technology.cariad.entertainment.metadataservice.common.codingprovider.data.MediaCodingValues
import technology.cariad.entertainment.metadataservice.common.codingprovider.service.IStorageConnection
import technology.cariad.entertainment.metadataservice.common.log.logger

internal class CodingParser {
    private val log by logger()

    var listener: IMediaCodingProviderListener? = null

    data class BlobData(
        val blobID: Int,
        var blobRequested: Boolean = false,
    )

    private val handledBlobs = arrayOf(
        BlobData(CodingConst.FUNCTIONCONFIGURATION_HMI_BLOB_ID)
    )

    fun requestBlobs(storageConnection: IStorageConnection) =
        handledBlobs.indices.forEach { i ->
            if (!handledBlobs[i].blobRequested) {
                handledBlobs[i].blobRequested = true
                storageConnection.getBlob(handledBlobs[i].blobID)
            }
        }

    fun parseBlob(blobID: Int, blobValue: ShortArray) {
        handledBlobs.find {
            it.blobID == blobID
        }?.let {
            when (it.blobID) {
                CodingConst.FUNCTIONCONFIGURATION_HMI_BLOB_ID -> {
                    parseForCountryOfSales(blobValue)
                }
            }

            MediaCodingValues.extraCoding[blobID] = blobValue
            listener?.onCodingBlobAvailable(blobID, blobValue)
        }
    }

    private fun parseForCountryOfSales(functionConfigurationHmiBlob: ShortArray) {

        log.info(this::parseForCountryOfSales.name) {
            "functionconfig HMI: ${functionConfigurationHmiBlob.contentToString()}"
        }

        val diagnosticsFirstByte = CodingConst.countryOfSalesFirstByte
        val diagnosticsSecondByte = CodingConst.countryOfSalesSecondByte

        if (functionConfigurationHmiBlob.size > diagnosticsSecondByte.byteWithValue) {
            val countryOfSalesCodingFirstByte = getCodingDecimalValue(
                functionConfigurationHmiBlob[diagnosticsFirstByte.byteWithValue].toInt(),
                diagnosticsFirstByte.startBit,
                diagnosticsFirstByte.length
            )

            val countryOfSalesCodingSecondByte = getCodingDecimalValue(
                functionConfigurationHmiBlob[diagnosticsSecondByte.byteWithValue].toInt(),
                diagnosticsSecondByte.startBit,
                diagnosticsSecondByte.length
            )

            log.info(this::parseForCountryOfSales.name) {
                "countryCode: first=${countryOfSalesCodingFirstByte.toChar()} " +
                        "second=${countryOfSalesCodingSecondByte.toChar()}"
            }

            val countryOfSalesCoding =
                "${countryOfSalesCodingFirstByte.toChar()}${countryOfSalesCodingSecondByte.toChar()}"

            log.info(this::parseForCountryOfSales.name) {
                "countryCode: $countryOfSalesCoding"
            }

            MediaCodingValues.countryOfSales = CountryOfSales(countryOfSalesCoding)

        } else {
            log.warn(this::parseForCountryOfSales.name) {
                "read blob size: ${functionConfigurationHmiBlob.size}"
            }
        }

        listener?.onCountryOfSalesAvailable()
    }

    /**
     * Get the actual value looking at the bits that contain the actual info of the coding
     * For example in: 00001010
     * For example, the byte value can be 10 if we take the value of the 8 bits, but 0 if we take
     * just the first 1 bit.
     */
    private fun getCodingDecimalValue(byteToAnalyze: Int, indexOfStartBit: Int, length: Int): Int {
        val binaryValue = toBinary(byteToAnalyze)
        val reversedBinaryValue =
            binaryValue.reversed()    // Reverse the binary value to crop the significant values
        // Select just the bits with the value
        val croppedBinaryValue = reversedBinaryValue.subSequence(
            startIndex = indexOfStartBit,
            endIndex = indexOfStartBit + length
        )

        // Convert the binary cropped value to its int value
        val decimalValue = croppedBinaryValue.toString().reversed().toInt(2)
        log.debug(this::getCodingDecimalValue.name) {
            "[coding_provider] Coding value in binary is: $binaryValue, " +
                    "croppedBinary value is: $croppedBinaryValue, " +
                    "Int final value is: $decimalValue"
        }
        return decimalValue
    }

    private fun toBinary(x: Int): String {
        return String.format(
            "%" + 8 + "s",
            Integer.toBinaryString(x)
        ).replace(" ".toRegex(), "0")
    }
}
