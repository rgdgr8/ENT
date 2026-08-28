package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.ExternalIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ExternalIdentifierEntity.Companion.OriginType
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier

class SampleExternalIdentifier {
    companion object {
        val EXTERNAL_IDENTIFIER_AMAZON_1 =
            ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_AMAZON, "amazon_id")
        val EXTERNAL_IDENTIFIER_ENTITY_AMAZON_1 = ExternalIdentifierEntity(
            OriginType.AMAZON,
            "amazon_id"
        )

        val EXTERNAL_IDENTIFIER_AMAZON_2 =
            ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_AMAZON, "amazon_id_2")
        val EXTERNAL_IDENTIFIER_ENTITY_AMAZON_2 = ExternalIdentifierEntity(
            OriginType.AMAZON,
            "amazon_id_2"
        )

        val EXTERNAL_IDENTIFIER_APPLE_1 =
            ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_APPLE, "apple_id")
        val EXTERNAL_IDENTIFIER_ENTITY_APPLE_1 = ExternalIdentifierEntity(
            OriginType.APPLE,
            "apple_id"
        )

        val EXTERNAL_IDENTIFIER_SPOTIFY_1 =
            ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_SPOTIFY, "spotify_id")
        val EXTERNAL_IDENTIFIER_ENTITY_SPOTIFY_1 = ExternalIdentifierEntity(
            OriginType.SPOTIFY,
            "spotify_id"
        )
    }
}

