package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.ProviderIdentifier
import de.esolutions.connectedradioclient.data.enums.Origin
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity

class ProviderIdentifierMapper : Mapper<ProviderIdentifier, ProviderIdentifierEntity>() {
    override fun mapValue(value: ProviderIdentifier): ProviderIdentifierEntity {

        val origin = when(value.origin) {
            Origin.RADIODNS -> ProviderIdentifierEntity.Companion.OriginType.RADIODNS
            Origin.RADIOPLAYER -> ProviderIdentifierEntity.Companion.OriginType.RADIOPLAYER
            Origin.GRACENOTE -> ProviderIdentifierEntity.Companion.OriginType.GRACENOTE
        }
        return ProviderIdentifierEntity(origin, value.externalId)
    }
}

