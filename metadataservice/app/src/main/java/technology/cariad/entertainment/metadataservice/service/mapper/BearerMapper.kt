package technology.cariad.entertainment.metadataservice.service.mapper

import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.DabBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IbocBearerEntity
import technology.cariad.entertainment.metadataserviceclient.api.StationBearer
import technology.cariad.entertainment.metadataserviceclient.util.DabBearer
import technology.cariad.entertainment.metadataserviceclient.util.FmRdsBearer
import technology.cariad.entertainment.metadataserviceclient.util.IbocBearer
import technology.cariad.entertainment.metadataserviceclient.util.TypedBearer

class BearerMapper : Mapper<TypedBearer, IBearerEntity>() {
    override fun mapValue(value: TypedBearer): IBearerEntity =
        when (value) {
            is FmRdsBearer -> FmRdsBearerEntity(value.pi, value.frequencyKhz, null, value.mcc)
            is DabBearer -> DabBearerEntity(value.sId, value.eId, value.scIds, value.ecc, value.mcc)
            is IbocBearer -> IbocBearerEntity(value.tx, value.cc, value.multicastId)
        }
}

class BearerEntityMapper : Mapper<IBearerEntity, StationBearer>() {
    override fun mapValue(value: IBearerEntity): StationBearer =
        when (value) {
            is FmRdsBearerEntity -> FmRdsBearer(
                value.pi,
                value.frequencyKhz,
                null,
                value.mcc
            ).rawBearer
            is DabBearerEntity -> DabBearer(
                value.sId,
                value.eId,
                value.scIds,
                value.ecc,
                value.mcc
            ).rawBearer
            is IbocBearerEntity -> IbocBearer(value.tx, value.cc, value.multicastId).rawBearer
            else -> throw IllegalArgumentException("") //TODO
        }
}
