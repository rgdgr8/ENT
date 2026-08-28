package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.ETagsEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataserviceclient.api.Station

class StationMapper : Mapper<StationEntity, Station>() {

    override fun mapValue(value: StationEntity): Station {

        val eTags = ETagsEntity(
            bearerListETag = value.bearerList?.etag ?: "",
            metadataETag = value.metadata?.etag ?: "",
            streamsETag = value.streamList?.etag ?: "",
            logoETag = value.logo?.eTag ?: "",
            logoSmallETag = value.logoSmall?.eTag ?: ""
        )

        return Station(
            metadata = value.metadata?.let { StationMetadataMapper().mapValue(it) },
            logo = value.logo?.let { StationLogoMapper().mapValue(it) },
            logoSmall = value.logoSmall?.let { StationLogoMapper().mapValue(it) },
            streams = StationStreamMapper().mapList(value.streamList?.streams ?: listOf()),
            bearers = BearerEntityMapper().mapList(value.bearerList?.bearers ?: listOf()),
            providerIdentifiers = ProviderIdentifierMapper().mapList(value.providerIdentifiers),
            lastModified = eTags.toString()
        )
    }
}
