package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Entity class for images.
 *
 * @param uri encoded uri according to RFC 2396 that can be used to obtain the image's binary data.
 * @param fileName file name of the image including extension
 * @param mimeType MIME type of the image
 * @param imageType type of the image
 * @param imageData the encoded image data
 * @param eTag e-tag identifying the current set of data
 */
data class ImageEntity(
    var uri: String? = null,
    var fileName: String,
    var imageType: ImageType? = ImageType.UNKNOWN,
    var imageData: List<Byte>?,
    var eTag: String? = null
) {
    companion object {
        enum class ImageType(val value: Int) {
            UNKNOWN(-1),
            LOGO(1),
            LOGO_SMALL(2),
            COVER_ART(3)
        }
    }

    override fun toString(): String {
        return "ImageEntity(uri=$uri," +
                " fileName='$fileName'," +
                " imageType=$imageType," +
                " imageSize=${imageData?.size}B)"
    }
}

