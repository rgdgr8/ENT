package technology.cariad.entertainment.metadataservice.data.datastore.radiostation

import technology.cariad.entertainment.metadataservice.data.database.dao.ProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.dao.StationProviderIdentifierDao
import technology.cariad.entertainment.metadataservice.data.database.entity.ProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.data.database.entity.StationProviderIdentifierEntry
import technology.cariad.entertainment.metadataservice.domain.entity.ProviderIdentifierEntity
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleProviderIdentifier
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val STATION_ID = 1L

class ProviderIdentifierStoreTest {

    private val providerIdentifierDao: ProviderIdentifierDao = mockk(relaxed = true)
    private val stationProviderIdentifierDao: StationProviderIdentifierDao = mockk(relaxed = true)

    private val providerIdentifierEntry1 = ProviderIdentifierEntry(
        origin = ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
        externalId = "Spotify"
    )
    private val providerIdentifierEntry2 = ProviderIdentifierEntry(
        origin = ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
        externalId = "Amazon"
    )

    lateinit var providerIdentifierStore: ProviderIdentifierStore

    @Before
    fun setup() {

        LoggerMock()

        providerIdentifierStore =
            ProviderIdentifierStore(providerIdentifierDao, stationProviderIdentifierDao)
    }

    @Test
    fun getProviderIdentifier_shouldReturnProviderIdentifierId() = runBlockingTest {

        every {
            providerIdentifierDao.createProviderIdentifierEntry(allAny())
        } returns providerIdentifierEntry1
        coEvery {
            providerIdentifierStore.selectOrInsertProviderIdentifier(providerIdentifierEntry1)
        } returns 1L

        val providerIdentifierId = providerIdentifierStore.getProviderIdentifier(
            SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1
        )

        Assert.assertEquals(1L, providerIdentifierId)

        verify {
            providerIdentifierDao.createProviderIdentifierEntry(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1
            )
        }
        coVerify {
            providerIdentifierStore.selectOrInsertProviderIdentifier(providerIdentifierEntry1)
        }
    }

    @Test
    fun selectOrInsertProviderIdentifier_shouldInsertProviderIdentifierIfNotFound() =
        runBlockingTest {

            coEvery {
                providerIdentifierDao.findIdByOriginAndExternalId(
                    allAny(),
                    allAny()
                )
            } returns null
            coEvery {
                providerIdentifierDao.insert(providerIdentifierEntry1)
            } returns 1L

            val providerIdentifierId =
                providerIdentifierStore.selectOrInsertProviderIdentifier(providerIdentifierEntry1)

            Assert.assertEquals(1L, providerIdentifierId)

            coVerify {
                providerIdentifierDao.findIdByOriginAndExternalId(
                    origin = ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                    externalId = "Spotify"
                )
                providerIdentifierDao.insert(providerIdentifierEntry1)
            }
        }

    @Test
    fun selectOrInsertProviderIdentifier_shouldReturnProviderIdentifierIdIfItFound() =
        runBlockingTest {

            coEvery {
                providerIdentifierDao.findIdByOriginAndExternalId(allAny(), allAny())
            } returns 1L

            val providerIdentifierId =
                providerIdentifierStore.selectOrInsertProviderIdentifier(providerIdentifierEntry1)

            Assert.assertEquals(1L, providerIdentifierId)

            coVerify {
                providerIdentifierDao.findIdByOriginAndExternalId(
                    origin = ProviderIdentifierEntity.Companion.OriginType.GRACENOTE,
                    externalId = "Spotify"
                )
            }
            coVerify(inverse = true) { providerIdentifierDao.insert(providerIdentifierEntry1) }
        }

    @Test
    fun insertProviderIdentifier_shouldInsertProviderIdentifierAndReturnItsId() = runBlockingTest {

        every { providerIdentifierDao.createProviderIdentifierEntry(allAny()) } returns providerIdentifierEntry1
        coEvery {
            providerIdentifierDao.selectOrInsertProvider(providerIdentifierEntry1)
        } returns 1L

        val providerIdentifierId =
            providerIdentifierStore.insertProviderIdentifier(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1
            )


        Assert.assertEquals(1L, providerIdentifierId)

        verify { providerIdentifierDao.createProviderIdentifierEntry(allAny()) }
        coVerify {
            providerIdentifierDao.selectOrInsertProvider(providerIdentifierEntry1)
        }
    }

    @Test
    fun insertProviderIdentifiers_shouldInsertProviderIdentifiersForStation() =
        runBlockingTest {

            val providerIdentifiers = listOf(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1,
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_1
            )
            val stationProviderIdentifiers = listOf(
                StationProviderIdentifierEntry(STATION_ID, 1),
                StationProviderIdentifierEntry(STATION_ID, 2)
            )

            val providerIdentifierStoreSpy = spyk(providerIdentifierStore)

            every {
                providerIdentifierDao.createProviderIdentifierEntry(any())
            } returns providerIdentifierEntry1 andThen providerIdentifierEntry2
            coEvery {
                providerIdentifierStoreSpy.selectOrInsertProviderIdentifier(any())
            } returns 1L andThen 2L
            every {
                stationProviderIdentifierDao.createStationProviderIdentifierEntry(
                    any(),
                    any()
                )
            } answers { callOriginal() }
            coEvery {
                stationProviderIdentifierDao.insert(any<List<StationProviderIdentifierEntry>>())
            } returns listOf<Long>(
                1L,
                2L
            )

            val providerIdentifierIds = providerIdentifierStoreSpy.insertProviderIdentifiers(
                STATION_ID,
                providerIdentifiers
            )

            Assert.assertEquals(listOf(1L, 2L), providerIdentifierIds)

            verify {
                providerIdentifierDao.createProviderIdentifierEntry(
                    SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1
                )
                providerIdentifierDao.createProviderIdentifierEntry(
                    SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_1
                )
                stationProviderIdentifierDao.createStationProviderIdentifierEntry(
                    STATION_ID,
                    1L
                )
                stationProviderIdentifierDao.createStationProviderIdentifierEntry(
                    STATION_ID,
                    2L
                )
            }
            coVerify {
                providerIdentifierStoreSpy.selectOrInsertProviderIdentifier(
                    providerIdentifierEntry1
                )
                providerIdentifierStoreSpy.selectOrInsertProviderIdentifier(
                    providerIdentifierEntry2
                )
                stationProviderIdentifierDao.insert(stationProviderIdentifiers)
            }
        }

    @Test
    fun updateProviderIdentifiers_shouldDeletePreviousStationProvidersAndInsertNewOnes() =
        runBlockingTest {

            val providerIdentifierEntities = listOf(
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_GRACENOTE_1,
                SampleProviderIdentifier.PROVIDER_IDENTIFIER_ENTITY_RADIODNS_1
            )

            val spiedProviderIdentifierStore = spyk(providerIdentifierStore)

            coEvery { stationProviderIdentifierDao.deleteByStationId(any()) } returns 1
            coEvery {
                spiedProviderIdentifierStore.insertProviderIdentifiers(
                    STATION_ID,
                    providerIdentifierEntities
                )
            } returns listOf<Long>()


            spiedProviderIdentifierStore.updateProviderIdentifiers(
                STATION_ID,
                providerIdentifierEntities
            )

            coVerify {
                stationProviderIdentifierDao.deleteByStationId(any())
                spiedProviderIdentifierStore.insertProviderIdentifiers(
                    STATION_ID,
                    providerIdentifierEntities
                )
            }
        }

    @Test
    fun updateProviderIdentifiers_shouldJustDeletePreviousStationProviderIdentifiersIfNewListEmpty() =
        runBlockingTest {

            val providerIdentifierEntities = listOf<ProviderIdentifierEntity>()

            coEvery { stationProviderIdentifierDao.deleteByStationId(any()) } returns 1

            providerIdentifierStore.updateProviderIdentifiers(
                STATION_ID,
                providerIdentifierEntities
            )

            coVerify { stationProviderIdentifierDao.deleteByStationId(any()) }
            coVerify(inverse = true) {
                providerIdentifierStore.insertProviderIdentifiers(
                    STATION_ID,
                    providerIdentifierEntities
                )
            }
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

