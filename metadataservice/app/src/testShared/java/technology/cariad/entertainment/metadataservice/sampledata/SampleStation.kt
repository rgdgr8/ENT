package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationEntity
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamListEntity
import technology.cariad.entertainment.metadataserviceclient.api.Station

class SampleStation {
    companion object {
        val STATION_1 = Station(
            SampleStationMetadata.STATION_METADATA_1,
            SampleStationLogo.STATION_LOGO_1,
            SampleStationLogo.STATION_LOGO_SMALL_1,
            listOf(SampleStationStream.STATION_STREAM_1),
            listOf(SampleBearer.FM_BEARER_1),
            "bearer_etag:metadata_etag:stream_etag:logo_etag:logo_small_etag",
            listOf(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_GRACENOTE_1,
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_RADIODNS_1
            )
        )
        val STATION_ENTITY_1
            get() = StationEntity(
                SampleStationMetadata.STATION_METADATA_ENTITY_1,
                SampleStationLogo.IMAGE_ENTITY_1,
                SampleStationLogo.IMAGE_ENTITY_SMALL_1,
                StationStreamListEntity( listOf(SampleStationStream.STATION_STREAM_ENTITY_1), "stream_etag"),
                BearerListEntity(listOf(SampleBearer.FM_BEARER_ENTITY_1), "bearer_etag"),
                listOf(
                    SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1,
                    SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_1
                ),
                SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
            )

        val STATION_2 = Station(
            SampleStationMetadata.STATION_METADATA_2,
            SampleStationLogo.STATION_LOGO_2,
            SampleStationLogo.STATION_LOGO_SMALL_2,
            listOf(SampleStationStream.STATION_STREAM_2),
            listOf(SampleBearer.FM_BEARER_2),
            "bearer_etag:metadata_etag:stream_etag:logo_etag:logo_small_etag",
            listOf(SampleProviderIdentifier.PROVIDER_IDENTIFIER_RADIODNS_2)
        )
        val STATION_ENTITY_2
            get() = StationEntity(
                SampleStationMetadata.STATION_METADATA_ENTITY_2,
                SampleStationLogo.IMAGE_ENTITY_2,
                SampleStationLogo.IMAGE_ENTITY_SMALL_2,
                StationStreamListEntity(listOf(SampleStationStream.STATION_STREAM_ENTITY_2), "stream_etag"),
                BearerListEntity(listOf(SampleBearer.FM_BEARER_ENTITY_2), "bearer_etag"),
                listOf(SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_2),
                SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
            )

        val STATION_NULL_1 = Station(
            null,
            null,
            null,
            listOf(),
            listOf(),
            "::::",
            listOf()
        )
        val STATION_NULL_ENTITY_1
            get() = StationEntity(
                null,
                null,
                null,
                StationStreamListEntity(listOf(), null),
                BearerListEntity(listOf(), null),
                listOf(),
                SampleLifeCycleData.LIFE_CYCLE_DATA_ENTITY_1
            )

        val STATION_ENTITY_3
            get() = StationEntity(
                SampleStationMetadata.STATION_METADATA_ENTITY_2,
                SampleStationLogo.IMAGE_ENTITY_2,
                SampleStationLogo.IMAGE_ENTITY_SMALL_2,
                StationStreamListEntity(listOf(SampleStationStream.STATION_STREAM_ENTITY_2), "stream_etag"),
                BearerListEntity(listOf(SampleBearer.FM_BEARER_ENTITY_2), "bearer_etag"),
                listOf(SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_2),
                null
            )
    }
}

