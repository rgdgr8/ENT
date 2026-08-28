package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataserviceclient.api.ProviderIdentifier
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity

class ProviderIdentifierMapper : Mapper<ProviderIdentifierEntity, ProviderIdentifier>() {
    override fun mapValue(value: ProviderIdentifierEntity): ProviderIdentifier =
        ProviderIdentifier(value.originType.ordinal, value.id)
}

