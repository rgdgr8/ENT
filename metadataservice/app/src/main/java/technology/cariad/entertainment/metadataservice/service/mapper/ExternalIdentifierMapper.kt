package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.ExternalIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ExternalIdentifierEntity.Companion.OriginType
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier.Companion.ORIGIN_TYPE_AMAZON
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier.Companion.ORIGIN_TYPE_APPLE
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier.Companion.ORIGIN_TYPE_SPOTIFY

class ExternalIdentifierMapper : Mapper<ExternalIdentifierEntity, ExternalIdentifier>() {
    override fun mapValue(value: ExternalIdentifierEntity): ExternalIdentifier =
        ExternalIdentifier(
            when (value.originType) {
                OriginType.AMAZON -> ORIGIN_TYPE_AMAZON
                OriginType.APPLE -> ORIGIN_TYPE_APPLE
                OriginType.SPOTIFY -> ORIGIN_TYPE_SPOTIFY
            }, value.id
        )
}

