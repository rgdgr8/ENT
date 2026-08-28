package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaEntity
import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaType
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia.Companion.TYPE_AM
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia.Companion.TYPE_FM

class BearerAsiaMapper : Mapper<StationBearerAsia, BearerAsiaEntity>() {
    override fun mapValue(value: StationBearerAsia): BearerAsiaEntity {

        val type = when (value.type) {
            TYPE_FM -> BearerAsiaType.FM
            TYPE_AM -> BearerAsiaType.AM
            else -> throw IllegalArgumentException("Invalid radio station bearer type in asia.")
        }
        return BearerAsiaEntity(type = type, frequencyKhz = value.frequencyKhz, mcc = value.mcc)
    }
}

