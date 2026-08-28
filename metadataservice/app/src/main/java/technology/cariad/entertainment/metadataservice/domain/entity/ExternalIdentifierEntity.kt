package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Represents an external identifier linking to contents from third parties.
 *
 * @param originType the origin of the external identifier
 * @param id the external identifier itself
 */
data class ExternalIdentifierEntity(
    val originType: OriginType,
    val id: String
) {
    companion object {
        enum class OriginType {
            APPLE,
            AMAZON,
            SPOTIFY
        }
    }
}
