package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaEntity
import technology.cariad.entertainment.metadataservice.domain.entity.BearerAsiaType
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia.Companion.TYPE_AM
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia.Companion.TYPE_FM

class SampleBearerAsia {
    companion object {
        val BEARER_ASIA_FM_1 = StationBearerAsia(TYPE_FM, "12345", "400")
        val BEARER_ASIA_FM_ENTITY_1 = BearerAsiaEntity(BearerAsiaType.FM, "12345", "400")

        val BEARER_ASIA_AM_1 = StationBearerAsia(TYPE_AM, "01234", "401")
        val BEARER_ASIA_AM_ENTITY_1 = BearerAsiaEntity(BearerAsiaType.AM, "01234", "401")

        val BEARER_ASIA_INVALID_TYPE = StationBearerAsia(3, "12345", "400")
    }
}

