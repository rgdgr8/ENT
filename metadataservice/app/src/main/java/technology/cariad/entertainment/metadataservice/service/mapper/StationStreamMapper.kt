package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataserviceclient.api.StationStream
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.StationStreamEntity

class StationStreamMapper : Mapper<StationStreamEntity, StationStream>() {
    override fun mapValue(value: StationStreamEntity): StationStream = StationStream(
        value.url,
        value.offsetMillis,
        value.quality.value
    )
}
