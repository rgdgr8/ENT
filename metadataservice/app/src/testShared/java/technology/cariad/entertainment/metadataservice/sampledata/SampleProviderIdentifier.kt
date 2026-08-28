package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataserviceclient.api.ProviderIdentifier
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity.Companion.OriginType

class SampleProviderIdentifier {
    companion object {
        val PROVIDER_IDENTIFIER_GRACENOTE_1 =
            ProviderIdentifier(ProviderIdentifier.ORIGIN_TYPE_GRACENOTE, "gracenote_id")
        val PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1 = ProviderIdentifierEntity(
            OriginType.GRACENOTE,
            "gracenote_id"
        )

        val PROVIDER_IDENTIFIER_RADIODNS_1 =
            ProviderIdentifier(ProviderIdentifier.ORIGIN_TYPE_RADIODNS, "radiodns_id")
        val PROVIDER_IDENTIFIER_ENTITY_RADIODNS_1 = ProviderIdentifierEntity(
            OriginType.RADIODNS,
            "radiodns_id"
        )

        val PROVIDER_IDENTIFIER_GRACENOTE_2 =
            ProviderIdentifier(ProviderIdentifier.ORIGIN_TYPE_GRACENOTE, "gracenote_id_2")
        val PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_2 = ProviderIdentifierEntity(
            OriginType.GRACENOTE,
            "gracenote_id_2"
        )

        val PROVIDER_IDENTIFIER_RADIODNS_2 =
            ProviderIdentifier(ProviderIdentifier.ORIGIN_TYPE_RADIODNS, "radiodns_id_2")
        val PROVIDER_IDENTIFIER_ENTITY_RADIODNS_2 = ProviderIdentifierEntity(
            OriginType.RADIODNS,
            "radiodns_id_2"
        )
    }
}

