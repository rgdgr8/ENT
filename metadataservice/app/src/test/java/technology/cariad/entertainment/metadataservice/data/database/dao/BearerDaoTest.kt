package technology.cariad.entertainment.metadataservice.data.database.dao

import technology.cariad.entertainment.metadataservice.domain.entity.BearerListEntity
import technology.cariad.entertainment.metadataservice.sampledata.SampleBearer
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class BearerDaoTest {

    private val bearerDao: BearerDao = mockk()

    @Test
    fun createBearerEntries_shouldConvertEntitiesToEntries() {

        every { bearerDao.createBearerEntries(any(), any()) } answers { callOriginal() }

        val bearerEntries = bearerDao.createBearerEntries(
            BearerListEntity(SampleBearer.BEARER_ENTITIES, "etag"), 1
        )

        bearerEntries.forEachIndexed { index, it ->
            Assert.assertEquals(SampleBearer.BEARER_STRINGS[index], it.uri)
            Assert.assertEquals(1, it.stationId)
            Assert.assertEquals(0, it.bearerId)
        }
    }
}

