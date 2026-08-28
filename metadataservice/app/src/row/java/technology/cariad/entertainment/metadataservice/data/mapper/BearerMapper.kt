package technology.cariad.entertainment.metadataservice.data.mapper

import de.esolutions.connectedradioclient.data.bearer.DabBearer
import de.esolutions.connectedradioclient.data.bearer.FmBearer
import de.esolutions.connectedradioclient.data.bearer.HdBearer
import de.esolutions.connectedradioclient.data.bearer.IBearer
import technology.cariad.entertainment.metadataservice.domain.common.Mapper
import technology.cariad.entertainment.metadataservice.domain.entity.DabBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IbocBearerEntity

class BearerMapper : Mapper<IBearerEntity, IBearer>() {
    override fun mapValue(value: IBearerEntity): IBearer =
        when (value) {
            is FmRdsBearerEntity -> FmBearer(value.mcc, value.pi, value.frequencyKhz)
            is DabBearerEntity -> DabBearer(value.mcc, value.ecc, value.eId, value.sId, value.scIds)
            is IbocBearerEntity -> HdBearer(value.tx, value.cc, value.multicastId ?: "1")
            else -> throw IllegalArgumentException("") //TODO
        }
}

class BearerEntityMapper : Mapper<IBearer, IBearerEntity>() {
    override fun mapValue(value: IBearer): IBearerEntity =
        when (value) {
            is FmBearer -> FmRdsBearerEntity(value.pi, value.frequency, null, value.mcc)
            is DabBearer -> DabBearerEntity(value.sid, value.eid, value.scids, value.ecc, value.mcc)
            is HdBearer -> IbocBearerEntity(value.tx, value.cc, value.multicastId ?: "1")
            else -> throw IllegalArgumentException("") //TODO
        }
}
