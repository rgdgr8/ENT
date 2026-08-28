package de.esolutions.connectedradioclient.data

import java.util.Locale

data class StationLogo(
    val imageData: ByteArray,
    val fileName: String,
    val fileExtension: String
) {

    companion object {

        /**
         * Create a [StationLogo] object from backend [disposition] (Filename), [mimeType] and [body]
         */
        fun createFromBackendData(body: ByteArray, disposition: String?, mimeType: String?): StationLogo? {
            val fileName = disposition?.let { parseFileName(it) }
            val fileExtension = mimeType?.let { parseFileExtension(it) }

            if (fileName == null || fileExtension == null) {
                return null
            }

            return StationLogo(
                body,
                fileName,
                fileExtension
            )
        }

        /**
         * Input: disposition => filename="123456-123456.webp"
         * 1. split at '"' => [filename=, 123456-123456.webp]
         * 2. split at '.' => [123456-123456, webp]
         * 3. Return first index
         */
        private fun parseFileName(disposition: String): String? {
            val splitDisposition = disposition.split('"')

            if (splitDisposition.size == 3) {
                val splitFileName = splitDisposition[1].split(".")

                if (splitFileName.size == 2) {
                    return splitFileName[0].ifEmpty { null }
                }
            }

            return null
        }

        /**
         * Input: mimeType = image/webp
         * 1. split at "/" => [image, webp]
         * 2. Return second index as uppercase => "WEBP"
         */
        private fun parseFileExtension(mimeType: String): String? {
            val splitMimeType = mimeType.split("/")
            if (splitMimeType.size == 2) {
                return splitMimeType[1].uppercase(Locale.ENGLISH).ifEmpty { null }
            }

            return null
        }
    }
}