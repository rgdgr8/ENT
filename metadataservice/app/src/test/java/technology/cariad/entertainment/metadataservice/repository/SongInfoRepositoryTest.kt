package technology.cariad.entertainment.metadataservice.repository

import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.common.servicelist.ServiceListEntry
import technology.cariad.entertainment.metadataservice.common.util.Error
import technology.cariad.entertainment.metadataservice.common.util.ServiceDisabledException
import technology.cariad.entertainment.metadataservice.common.util.Success
import technology.cariad.entertainment.metadataservice.data.provider.OnlineState
import technology.cariad.entertainment.metadataservice.domain.IImageDataStore
import technology.cariad.entertainment.metadataservice.domain.IOnlineStateProvider
import technology.cariad.entertainment.metadataservice.domain.ISongInfoProvider
import technology.cariad.entertainment.metadataservice.domain.LookupMode
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SongInfoRepositoryTest {

    init {
        LoggerMock()
    }

    private val songInfoProviderMock = mockk<ISongInfoProvider>()
    private val imageDataStoreMock = mockk<IImageDataStore>()
    private var onlineStateProviderMock = mockk<IOnlineStateProvider>(relaxed = true)

    private val onlineStateMock = mockk<OnlineState>(relaxed = true)

    private lateinit var songInfoRepository: SongInfoRepository

    @Before
    fun setup() {
        every { songInfoProviderMock.serviceId } returns ServiceIdentifier.GRACENOTE_V1
        every { onlineStateProviderMock.onlineStateFlow(any()) } returns
                flowOf(onlineStateMock)

        every { songInfoProviderMock.selectNetwork(any()) } just Runs
        every { songInfoProviderMock.setServiceConfig(any()) } just Runs

        songInfoRepository = SongInfoRepository(
            songInfoProviderMock,
            imageDataStoreMock,
            onlineStateProviderMock
        )
    }

    @Test
    fun retrieveSongInfo_shouldForwardEmptyResultFromOnlineProvider() = runBlocking {
        every { onlineStateProviderMock.isOnlineLookupAllowed(ServiceIdentifier.GRACENOTE_V1) } returns LookupMode.FULL
        every { songInfoProviderMock.getSongInfo() } returns flowOf(
            Pair(SongInfoEntity(), ImageEntity(fileName = "", imageData = null))
        )

        val emptyResult = songInfoRepository.retrieveSongInfo(false).first()

        Assert.assertEquals(Success(SongInfoEntity()), emptyResult)
    }

    @Test
    fun retrieveSongInfo_shouldLoadImageIfPresent() = runBlocking {
        every { onlineStateProviderMock.isOnlineLookupAllowed(ServiceIdentifier.GRACENOTE_V1) } returns LookupMode.FULL
        every { songInfoProviderMock.getSongInfo() } returns flowOf(
            Pair(
                SongInfoEntity(),
                ImageEntity(fileName = "test_image", imageData = listOf(0x12, 0x34))
            )
        )
        every { imageDataStoreMock.loadImageUri(any()) } returns "valid_uri"

        songInfoRepository.retrieveSongInfo(false).first()

        verify {
            imageDataStoreMock.loadImageUri(any())
        }

        verify(inverse = true) {
            imageDataStoreMock.saveImage(any(), any())
        }

        confirmVerified(imageDataStoreMock)
    }

    @Test
    fun retrieveSongInfo_shouldSaveImageIfMissing() = runBlocking {
        every { onlineStateProviderMock.isOnlineLookupAllowed(ServiceIdentifier.GRACENOTE_V1) } returns LookupMode.FULL
        every { songInfoProviderMock.getSongInfo() } returns flowOf(
            Pair(
                SongInfoEntity(),
                ImageEntity(fileName = "test_image", imageData = listOf(0x12, 0x34))
            )
        )
        every { imageDataStoreMock.loadImageUri(any()) } returns null
        every { imageDataStoreMock.saveImage(any(), any()) } returns "valid_uri"

        songInfoRepository.retrieveSongInfo(false).first()

        verifyOrder {
            imageDataStoreMock.loadImageUri(any())
            imageDataStoreMock.saveImage(any(), any())
        }

        confirmVerified(imageDataStoreMock)
    }

    @Test
    fun retrieveSongInfo_shouldDiscardLastResultAfterUnsubscribe() = runBlocking {
        every { onlineStateProviderMock.isOnlineLookupAllowed(ServiceIdentifier.GRACENOTE_V1) } returns LookupMode.FULL
        every { imageDataStoreMock.loadImageUri(any()) } returns "uri"

        every { songInfoProviderMock.getSongInfo() } returns flowOf(
            Pair(
                SongInfoEntity(artist = "artist", title = "title", album = "album"),
                ImageEntity(fileName = "test_image", imageData = listOf(0x12, 0x34))
            )
        ) andThen flowOf()

        val first = songInfoRepository.retrieveSongInfo(false).first()
        val second = withTimeoutOrNull(10) {
            songInfoRepository.retrieveSongInfo(false).firstOrNull()
        }

        Assert.assertNotEquals(first, second)
        Assert.assertNull(second)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}

