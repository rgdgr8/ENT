package technology.cariad.entertainment.metadataservice.domain.entity

/**
 * Represents a provider specific identifier to denote the origin of externally retrieved data.
 *
 * @param originType the origin of the provider specific identifier
 * @param id the provider specific identifier itself
 */
data class ProviderIdentifierEntity(
    val originType: OriginType,
    val id: String
) {
    companion object {
        enum class OriginType(val value: Int) {
            UNKNOWN(-1),
            RADIODNS(1),
            RADIOPLAYER(2),
            GRACENOTE(3)
        }
    }
}
