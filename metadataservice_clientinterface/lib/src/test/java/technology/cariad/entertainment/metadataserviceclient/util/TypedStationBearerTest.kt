package technology.cariad.entertainment.metadataserviceclient.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TypedStationBearerTest {

    @Test
    fun conversionOfFmRdsBearer_ShouldNotLooseInformation() {
        val inputNullEcc = FmRdsBearer("pi", "freq", null, "mcc")
        val outputNullEcc = TypedBearer.fromRawBearer(inputNullEcc.rawBearer)
        assertThat(outputNullEcc).isEqualTo(inputNullEcc)

        val inputWithEcc = FmRdsBearer("pi", "freq", "ecc", "mcc")
        val outputWithEcc = TypedBearer.fromRawBearer(inputWithEcc.rawBearer)
        assertThat(outputWithEcc).isEqualTo(inputWithEcc)
    }

    @Test
    fun conversionOfDabBearer_ShouldNotLooseInformation() {
        val input = DabBearer("sId", "eId", "scIds", "ecc", "mcc")
        val output = TypedBearer.fromRawBearer(input.rawBearer)
        assertThat(output).isEqualTo(input)
    }

    @Test
    fun conversionOfIbocBearer_ShouldNotLooseInformation() {
        val inputWithMulticastId = IbocBearer("tx", "cc", "multicastId")
        val outputWithMulticastId = TypedBearer.fromRawBearer(inputWithMulticastId.rawBearer)
        assertThat(outputWithMulticastId).isEqualTo(inputWithMulticastId)
    }
}

