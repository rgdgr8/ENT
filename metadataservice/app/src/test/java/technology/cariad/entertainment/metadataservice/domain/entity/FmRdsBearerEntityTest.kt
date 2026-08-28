package technology.cariad.entertainment.metadataservice.domain.entity

import org.junit.Assert
import org.junit.Test

class FmRdsBearerEntityTest {

    @Test
    fun fmRdsBearerEntityConstructor_shouldCreateEntryWithoutOptionalParams() {

        val fmRdsBearerEntity = FmRdsBearerEntity(pi = "pi", mcc = "mcc", frequencyKhz = "freq")

        Assert.assertEquals(null, fmRdsBearerEntity.ecc)
        Assert.assertEquals("freq", fmRdsBearerEntity.frequencyKhz)
        Assert.assertEquals("mcc", fmRdsBearerEntity.mcc)
        Assert.assertEquals("pi", fmRdsBearerEntity.pi)
    }
}
