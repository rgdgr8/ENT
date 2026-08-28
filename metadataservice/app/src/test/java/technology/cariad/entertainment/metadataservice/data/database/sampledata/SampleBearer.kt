package technology.cariad.entertainment.metadataservice.data.database.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.DabBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IbocBearerEntity

class SampleBearer {

    companion object {
        @JvmField
        val FM_BEARER_ENTITY_1 = FmRdsBearerEntity("c586", "09580", "e1", "208")
        const val FM_BEARER_STRING_1 = "fm:c586.09580.208.e1"

        @JvmField
        val FM_BEARER_ENTITY_2 = FmRdsBearerEntity("d1e0", "10390", "e0", "262")
        const val FM_BEARER_STRING_2 = "fm:d1e0.10390.262.e0"

        @JvmField
        val FM_BEARER_ENTITY_3 = FmRdsBearerEntity("c586", "09580", null, "208")
        const val FM_BEARER_STRING_3 = "fm:c586.09580.208"

        @JvmField
        val FM_BEARER_ENTITY_4 = FmRdsBearerEntity("d1e0", "10390", null, "262")
        const val FM_BEARER_STRING_4 = "fm:d1e0.10390.262"

        @JvmField
        val DAB_BEARER_ENTITY_1 = DabBearerEntity("d220", "100c", "0", "e0", "262")
        const val DAB_BEARER_STRING_1 = "dab:100c.d220.0.262.e0"

        @JvmField
        val DAB_BEARER_ENTITY_2 = DabBearerEntity("cc86", "c18c", "0", "e1", "262")
        const val DAB_BEARER_STRING_2 = "dab:c18c.cc86.0.262.e1"

        @JvmField
        val IBOC_BEARER_ENTITY_1 = IbocBearerEntity("07426", "292", "2")
        const val IBOC_BEARER_STRING_1 = "hd:07426.292.2"

        @JvmField
        val FK_BEARER_ENTITY_1 = FakeBearerEntitiy("c586", "09680", "e1", "208")
        const val FK_BEARER_STRING_1 = "fk:c586.09580.208.e1"

        @JvmField
        val FK_BEARER_ENTITY_2 = FakeBearerEntitiy("c586", "09780", "e1", "208")
        const val FK_BEARER_STRING_2 = "fk:c586.09580.208.e1"

        @JvmField
        val FK_BEARER_ENTITY_3 = FakeBearerEntitiy("c586", "09880", "e1", "208")
        const val FK_BEARER_STRING_3 = "fk:c586.09580.208.e1"

        @JvmField
        val BEARER_ENTITIES = listOf(
            FM_BEARER_ENTITY_1,
            FM_BEARER_ENTITY_2,
            FM_BEARER_ENTITY_3,
            FM_BEARER_ENTITY_4,
            DAB_BEARER_ENTITY_1,
            DAB_BEARER_ENTITY_2,
            IBOC_BEARER_ENTITY_1
        )

        @JvmField
        val BEARER_STRINGS = listOf(
            FM_BEARER_STRING_1,
            FM_BEARER_STRING_2,
            FM_BEARER_STRING_3,
            FM_BEARER_STRING_4,
            DAB_BEARER_STRING_1,
            DAB_BEARER_STRING_2,
            IBOC_BEARER_STRING_1
        )
    }
}

