package technology.cariad.entertainment.metadataservice.data.database.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.IBearerEntity

data class FakeBearerEntitiy(
    val pi: String,
    val frequencyKhz: String,
    val ecc: String? = null,
    val mcc: String
) : IBearerEntity
