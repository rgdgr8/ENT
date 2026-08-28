package technology.cariad.entertainment.metadataservice.domain.entity

import org.junit.Assert
import org.junit.Test

class IbocBearerEntityTest {

    @Test
    fun ibocBearerEntityConstructor_shouldCreateEntryWithoutOptionalParams() {

        val ibocBearerEntity = IbocBearerEntity(tx="tx", cc = "cc", multicastId = "mid")

        Assert.assertEquals("tx", ibocBearerEntity.tx)
        Assert.assertEquals("cc", ibocBearerEntity.cc)
        Assert.assertEquals("mid", ibocBearerEntity.multicastId)
    }
}
