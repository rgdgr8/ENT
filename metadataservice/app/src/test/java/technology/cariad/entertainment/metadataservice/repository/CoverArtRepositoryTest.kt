package technology.cariad.entertainment.metadataservice.repository

import android.net.Network
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.util.ServiceDisabledException
import technology.cariad.entertainment.metadataservice.data.database.CachingDatabase
import technology.cariad.entertainment.metadataservice.data.provider.OnlineState
import technology.cariad.entertainment.metadataservice.domain.ICoverArtDataStore
import technology.cariad.entertainment.metadataservice.domain.ICoverArtProvider
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.ITimeProvider
import technology.cariad.entertainment.metadataservice.domain.LookupMode
import technology.cariad.entertainment.metadataservice.domain.entity.CoverArtRequestDataEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.android.TransactionMock
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import technology.cariad.entertainment.metadataservice.sampledata.SampleImage.Companion.IMAGE_ENTITY_2
import technology.cariad.entertainment.metadataservice.sampledata.SampleImage.Companion.IMAGE_ENTITY_WITH_IMAGE_DATA
import io.mockk.Runs
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

private const val FAKE_URI = "uri"

class CoverArtRepositoryTest {

    init {
        LoggerMock()
    }

    private val database: CachingDatabase = mockk(relaxed = true)
    private val coverArtProvider: ICoverArtProvider = mockk(relaxed = true)
    private val coverArtDataStore: ICoverArtDataStore = mockk(relaxed = true)
    private val onlineStateProvider: IOnlineStateProvider = mockk(relaxed = true)
    private val timeProvider: ITimeProvider = mockk(relaxed = true)

    private val networkMock = mockk<NetworkInfo>()
    private val onlineStateMock = mockk<OnlineState>(relaxed = true)

    private lateinit var coverArtRepository: CoverArtRepository

    @Before
    fun setup() {
        coEvery { coverArtProvider.serviceId } returns ServiceIdentifier.GRACENOTE_V1

        coverArtRepository =
            CoverArtRepository(
                coverArtProvider,
                coverArtDataStore,
                onlineStateProvider,
                timeProvider
            )

        TransactionMock(database)
        LoggerMock()
    }

    @Test
    fun retrieveCoverArtImage_shouldReturnUriOfAlreadyCachedImage() =
        runBlockingTest {
            every { onlineStateProvider.onlineStateFlow(any(), any()) } returns flow {
                emit(onlineStateMock)
            }
            every { onlineStateMock.networkInfo } returns null

            coEvery { coverArtDataStore.getCoverArt(any()) } returns IMAGE_ENTITY_2

            val uri = coverArtRepository.retrieveCoverArtImage(
                CoverArtRequestDataEntity(
                    artist = "Madonna",
                    album = "Like a Prayer",
                    title = "Cherish",
                    lifeCycleData = null
                )
            )

            Assert.assertEquals(FAKE_URI, uri)

            coVerify { coverArtDataStore.getCoverArt(any()) }
            coVerify(inverse = true) { coverArtProvider.getCoverArtImage(any()) }
        }

    @Test
    fun retrieveCoverArtImage_shouldNotGetImageViaOnlineWhenDisabled() =
        runBlockingTest {
            every { onlineStateMock.onlineLookupEnabled } returns false
            every { onlineStateProvider.onlineStateFlow(any(), any()) } returns flow {
                emit(onlineStateMock)
            }
            every { onlineStateMock.networkInfo } returns networkMock

            coEvery { coverArtDataStore.getCoverArt(any()) } returns null
            coEvery { coverArtProvider.getCoverArtImage(any()) } returns IMAGE_ENTITY_2

            val uri = coverArtRepository.retrieveCoverArtImage(
                CoverArtRequestDataEntity(
                    artist = "Madonna",
                    album = "Like a Prayer",
                    title = "Cherish",
                    lifeCycleData = null
                )
            )

            Assert.assertEquals(null, uri)

            coVerify {
                coverArtDataStore.getCoverArt(any())
            }

            coVerify(exactly = 0) {
                coverArtProvider.getCoverArtImage(any())
            }
        }

    @Test
    fun retrieveCoverArtImage_shouldReturnNullIfNotCachedAndOnlineImageDataNotValid() =
        runBlockingTest {
            every { onlineStateMock.onlineLookupEnabled } returns true
            every { onlineStateProvider.onlineStateFlow(any(), any()) } returns flow {
                emit(onlineStateMock)
            }
            every { onlineStateMock.networkInfo } returns networkMock

            coEvery { coverArtDataStore.getCoverArt(any()) } returns null
            coEvery { coverArtProvider.getCoverArtImage(any()) } returns IMAGE_ENTITY_2

            val uri = coverArtRepository.retrieveCoverArtImage(
                CoverArtRequestDataEntity(
                    artist = "Madonna",
                    album = "Like a Prayer",
                    title = "Cherish",
                    lifeCycleData = null
                )
            )

            Assert.assertEquals(null, uri)

            coVerify {
                coverArtDataStore.getCoverArt(any())
                coverArtProvider.getCoverArtImage(any())
            }

            coVerify(exactly = 0) {
                coverArtDataStore.addCoverArtLookup(any(), any())
            }

        }

    @Test
    fun retrieveCoverArtImage_shouldReturnValidUriIfNotCachedButOnlineImageDataValid() =
        runBlocking {
            every { onlineStateMock.onlineLookupEnabled } returns true
            every { onlineStateProvider.onlineStateFlow(any(), any()) } returns flow {
                emit(onlineStateMock)
            }
            every { onlineStateMock.networkInfo } returns networkMock

            coEvery { coverArtDataStore.getCoverArt(any()) } returns null
            coEvery {
                coverArtProvider.getCoverArtImage(any())
            } returns IMAGE_ENTITY_WITH_IMAGE_DATA
            coEvery { coverArtDataStore.addCoverArtLookup(any(), any()) } returns FAKE_URI

            val uri = coverArtRepository.retrieveCoverArtImage(
                CoverArtRequestDataEntity(
                    artist = "Madonna",
                    album = "Like a Prayer",
                    title = "Cherish",
                    lifeCycleData = null
                )
            )

            Assert.assertEquals(FAKE_URI, uri)

            coVerify {
                coverArtDataStore.getCoverArt(any())
                coverArtProvider.getCoverArtImage(any())
                coverArtDataStore.addCoverArtLookup(any(), any())
            }
        }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

