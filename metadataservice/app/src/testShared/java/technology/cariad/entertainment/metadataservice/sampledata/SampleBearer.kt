package technology.cariad.entertainment.metadataservice.sampledata

import technology.cariad.entertainment.metadataservice.domain.entity.DabBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.FmRdsBearerEntity
import technology.cariad.entertainment.metadataservice.domain.entity.IbocBearerEntity
import technology.cariad.entertainment.metadataserviceclient.util.DabBearer
import technology.cariad.entertainment.metadataserviceclient.util.FmRdsBearer
import technology.cariad.entertainment.metadataserviceclient.util.IbocBearer

class SampleBearer {
    companion object {
        val FM_BEARER_1 = FmRdsBearer("c586", "09580", null, "208").rawBearer
        val FM_BEARER_ENTITY_1 = FmRdsBearerEntity("c586", "09580", null, "208")
        const val FM_BEARER_STRING_1 = "fm:c586.09580.208"

        val FM_BEARER_2 = FmRdsBearer("d1e0", "10390", null, "262").rawBearer
        val FM_BEARER_ENTITY_2 = FmRdsBearerEntity("d1e0", "10390", null, "262")
        const val FM_BEARER_STRING_2 = "fm:d1e0.10390.262"

        val FM_BEARER_3 = FmRdsBearer("c586", "09580", null, "208").rawBearer
        val FM_BEARER_ENTITY_3 = FmRdsBearerEntity("c586", "09580", null, "208")
        const val FM_BEARER_STRING_3 = "fm:c586.09580.208"

        val FM_BEARER_4 = FmRdsBearer("d1e0", "10390", null, "262").rawBearer
        val FM_BEARER_ENTITY_4 = FmRdsBearerEntity("d1e0", "10390", null, "262")
        const val FM_BEARER_STRING_4 = "fm:d1e0.10390.262"

        val DAB_BEARER_1 = DabBearer("d220", "100c", "0", "e0", "262").rawBearer
        val DAB_BEARER_ENTITY_1 = DabBearerEntity("d220", "100c", "0", "e0", "262")
        const val DAB_BEARER_STRING_1 = "dab:100c.d220.0.262.e0"

        val DAB_BEARER_2 = DabBearer("cc86", "c18c", "0", "e1", "262").rawBearer
        val DAB_BEARER_ENTITY_2 = DabBearerEntity("cc86", "c18c", "0", "e1", "262")
        const val DAB_BEARER_STRING_2 = "dab:c18c.cc86.0.262.e1"

        val IBOC_BEARER_1 = IbocBearer("07426", "292", "2").rawBearer
        val IBOC_BEARER_ENTITY_1 = IbocBearerEntity("07426","292", "2")
        const val IBOC_BEARER_STRING_1 = "hd:07426.292.2"

        val BEARERS = listOf(
            FM_BEARER_1,
            FM_BEARER_2,
            FM_BEARER_3,
            FM_BEARER_4,
            DAB_BEARER_1,
            DAB_BEARER_2,
            IBOC_BEARER_1
        )

        val BEARER_ENTITIES = listOf(
            FM_BEARER_ENTITY_1,
            FM_BEARER_ENTITY_2,
            FM_BEARER_ENTITY_3,
            FM_BEARER_ENTITY_4,
            DAB_BEARER_ENTITY_1,
            DAB_BEARER_ENTITY_2,
            IBOC_BEARER_ENTITY_1
        )

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

