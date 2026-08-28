package technology.cariad.entertainment.metadataserviceclient.api

import android.os.Handler
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val CLIENT_ID = 1

@RunWith(AndroidJUnit4::class)
class MetadataServiceClientAndroidTest {

    private val metadataServiceAidlMock = mockk<IMetadataServiceAidl>(relaxed = true)
    private val metadataServiceManagementMock = mockk<IMetadataServiceManagement>(relaxed = true)
    private val onlineLookupStateCallbackMock = mockk<IOnlineLookupStateCallback>(relaxed = true)
    private val songRecognitionCallbackMock = mockk<ISongRecognitionCallback>(relaxed = true)
    private val coverArtCallbackMock = mockk<ICoverArtCallback>(relaxed = true)
    private val permissionDataCallbackMock = mockk<IPermissionDataSingleCallback>(relaxed = true)
    private val songLyricsCallbackMock = mockk<ISongLyricsCallback>(relaxed = true)

    private val handlerMock = mockk<Handler>()

    private lateinit var metadataServiceClient: MetadataServiceClient

    @Before
    fun setup() {
        val runnableSlot = slot<Runnable>()
        every { handlerMock.post(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            true
        }

        every { metadataServiceAidlMock.management() } returns metadataServiceManagementMock
        excludeRecords { metadataServiceAidlMock.management() }

        every { metadataServiceManagementMock.registerClient(any(), any()) } returns CLIENT_ID
        excludeRecords { metadataServiceManagementMock.registerClient(any(), any()) }

        metadataServiceClient = MetadataServiceClient(metadataServiceAidlMock, handlerMock)
    }

    @Test
    fun constructor_shouldFowardRegisterClientCallToAIDLInterface() {
        clearMocks(metadataServiceManagementMock)

        MetadataServiceClient(metadataServiceAidlMock, handlerMock)

        verify { metadataServiceManagementMock.registerClient(any(), any()) }

        confirmVerified(metadataServiceManagementMock)
    }

    @Test
    fun unregisterClient_shouldFowardCallToAIDLInterface() {
        metadataServiceClient.unregisterClient()

        verify { metadataServiceManagementMock.unregisterClient(CLIENT_ID, any()) }

        confirmVerified(metadataServiceManagementMock)
    }

    @Test
    fun changeOnlineLookupStateSubscribe_shouldForwardCallToAIDLInterface() {
        val requestId = metadataServiceClient.changeOnlineLookupStateSubscribe(
            true,
            onlineLookupStateCallbackMock
        )

        verify {
            metadataServiceAidlMock.changeOnlineLookupStateSubscribe(
                CLIENT_ID, requestId, true, any()
            )
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun onlineLookupStateCallbackOnSuccess_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<BooleanSingleCallback>()
        every {
            metadataServiceAidlMock.changeOnlineLookupStateSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.changeOnlineLookupStateSubscribe(true, onlineLookupStateCallbackMock)

        aidlCallbackSlot.captured.onSuccess(true)

        verify {
            onlineLookupStateCallbackMock.onSuccess(true)
        }

        confirmVerified(onlineLookupStateCallbackMock)
    }

    @Test
    fun onlineLookupStateCallbackOnSuccess_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<BooleanSingleCallback>()
        every {
            metadataServiceAidlMock.changeOnlineLookupStateSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId = metadataServiceClient.changeOnlineLookupStateSubscribe(
            true, onlineLookupStateCallbackMock
        )
        metadataServiceClient.changeOnlineLookupStateDispose(requestId)

        aidlCallbackSlot.captured.onSuccess(true)

        verify {
            onlineLookupStateCallbackMock wasNot Called
        }

        confirmVerified(onlineLookupStateCallbackMock)
    }

    @Test
    fun onlineLookupStateCallbackOnError_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<BooleanSingleCallback>()
        every {
            metadataServiceAidlMock.changeOnlineLookupStateSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.changeOnlineLookupStateSubscribe(true, onlineLookupStateCallbackMock)

        aidlCallbackSlot.captured.onError("error")

        verify {
            onlineLookupStateCallbackMock.onError("error")
        }

        confirmVerified(onlineLookupStateCallbackMock)
    }

    @Test
    fun onlineLookupStateCallbackOnError_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<BooleanSingleCallback>()
        every {
            metadataServiceAidlMock.changeOnlineLookupStateSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId = metadataServiceClient.changeOnlineLookupStateSubscribe(
            true, onlineLookupStateCallbackMock
        )
        metadataServiceClient.changeOnlineLookupStateDispose(requestId)

        aidlCallbackSlot.captured.onError("error")

        verify {
            onlineLookupStateCallbackMock wasNot Called
        }

        confirmVerified(onlineLookupStateCallbackMock)
    }

    @Test
    fun changeOnlineLookupStateDispose_shouldFowardCallToAIDLInterfaceIfSubscribed() {
        val requestId = metadataServiceClient.changeOnlineLookupStateSubscribe(
            true,
            onlineLookupStateCallbackMock
        )

        metadataServiceClient.changeOnlineLookupStateDispose(requestId)

        verifySequence {
            metadataServiceAidlMock.changeOnlineLookupStateSubscribe(
                CLIENT_ID, requestId, true, any()
            )
            metadataServiceAidlMock.changeOnlineLookupStateDispose(CLIENT_ID, requestId)
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun changeOnlineLookupStateDispose_shouldDoNothingIfNotSubscribed() {
        metadataServiceClient.changeOnlineLookupStateDispose(0)

        verify {
            metadataServiceAidlMock wasNot Called
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun observeSongRecognitionSubscribe_shouldForwardCallToAIDLInterface() {
        val requestId =
            metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)

        verify {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, requestId, any()
            )
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun songRecognitionCallbackOnNext_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<RecognizedSongInfoCallback>()
        every {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)

        val recognizedSongInfo = RecognizedSongInfo(
            "artist",
            "title",
            "album",
            "year",
            "uri",
            "era",
            "genre",
            "mood",
            "tempo",
            listOf(ExternalIdentifier(0, "id")),
            "gnId",
            "gnUId",
            listOf(ExternalIdentifier(0, "album_id"))
        )
        aidlCallbackSlot.captured.onNext(recognizedSongInfo)

        verify {
            songRecognitionCallbackMock.onNext(recognizedSongInfo)
        }

        confirmVerified(songRecognitionCallbackMock)
    }

    @Test
    fun songRecognitionCallbackOnNext_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<RecognizedSongInfoCallback>()
        every {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId =
            metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)
        metadataServiceClient.observeSongRecognitionDispose(requestId)

        val recognizedSongInfo = RecognizedSongInfo(
            "artist",
            "title",
            "album",
            "year",
            "uri",
            "era",
            "genre",
            "mood",
            "tempo",
            listOf(ExternalIdentifier(0, "id")),
            "gnId",
            "gnUId",
            listOf(ExternalIdentifier(0, "album_id"))
        )
        aidlCallbackSlot.captured.onNext(recognizedSongInfo)

        verify {
            songRecognitionCallbackMock wasNot Called
        }

        confirmVerified(songRecognitionCallbackMock)
    }

    @Test
    fun songRecognitionCallbackOnComplete_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<RecognizedSongInfoCallback>()
        every {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)

        aidlCallbackSlot.captured.onComplete()

        verify {
            songRecognitionCallbackMock.onComplete()
        }

        confirmVerified(songRecognitionCallbackMock)
    }

    @Test
    fun songRecognitionCallbackOnComplete_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<RecognizedSongInfoCallback>()
        every {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId =
            metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)
        metadataServiceClient.observeSongRecognitionDispose(requestId)

        aidlCallbackSlot.captured.onComplete()

        verify {
            songRecognitionCallbackMock wasNot Called
        }

        confirmVerified(songRecognitionCallbackMock)
    }

    @Test
    fun songRecognitionCallbackOnError_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<RecognizedSongInfoCallback>()
        every {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)

        aidlCallbackSlot.captured.onError("error")

        verify {
            songRecognitionCallbackMock.onError("error")
        }

        confirmVerified(songRecognitionCallbackMock)
    }

    @Test
    fun songRecognitionCallbackOnError_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<RecognizedSongInfoCallback>()
        every {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId =
            metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)
        metadataServiceClient.observeSongRecognitionDispose(requestId)

        aidlCallbackSlot.captured.onError("error")

        verify {
            songRecognitionCallbackMock wasNot Called
        }

        confirmVerified(songRecognitionCallbackMock)
    }

    @Test
    fun observeSongRecognitionDispose_shouldFowardCallToAIDLInterfaceIfSubscribed() {
        val requestId =
            metadataServiceClient.observeSongRecognitionSubscribe(songRecognitionCallbackMock)

        metadataServiceClient.observeSongRecognitionDispose(requestId)

        verifySequence {
            metadataServiceAidlMock.observeSongRecognitionSubscribe(
                CLIENT_ID, requestId, any()
            )
            metadataServiceAidlMock.observeSongRecognitionDispose(CLIENT_ID, requestId)
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun observeSongRecognitionDispose_shouldDoNothingIfNotSubscribed() {
        metadataServiceClient.observeSongRecognitionDispose(0)

        verify {
            metadataServiceAidlMock wasNot Called
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun getCoverArtSubscribe_shouldForwardCallToAIDLInterface() {
        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        val requestId = metadataServiceClient.getCoverArtSubscribe(
            coverArtRequestData, coverArtCallbackMock
        )

        verify {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, requestId, coverArtRequestData, any()
            )
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun getCoverArtCallbackOnNext_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<StringCallback>()
        every {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        metadataServiceClient.getCoverArtSubscribe(coverArtRequestData, coverArtCallbackMock)

        aidlCallbackSlot.captured.onNext("uri")

        verify {
            coverArtCallbackMock.onNext("uri")
        }

        confirmVerified(coverArtCallbackMock)
    }

    @Test
    fun getCoverArtCallbackOnNext_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<StringCallback>()
        every {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        val requestId =
            metadataServiceClient.getCoverArtSubscribe(coverArtRequestData, coverArtCallbackMock)
        metadataServiceClient.getCoverArtDispose(requestId)

        aidlCallbackSlot.captured.onNext("uri")

        verify {
            coverArtCallbackMock wasNot Called
        }

        confirmVerified(coverArtCallbackMock)
    }

    @Test
    fun getCoverArtCallbackOnComplete_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<StringCallback>()
        every {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        metadataServiceClient.getCoverArtSubscribe(coverArtRequestData, coverArtCallbackMock)

        aidlCallbackSlot.captured.onComplete()

        verify {
            coverArtCallbackMock.onComplete()
        }

        confirmVerified(coverArtCallbackMock)
    }

    @Test
    fun getCoverArtCallbackOnComplete_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<StringCallback>()
        every {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        val requestId =
            metadataServiceClient.getCoverArtSubscribe(coverArtRequestData, coverArtCallbackMock)
        metadataServiceClient.getCoverArtDispose(requestId)

        aidlCallbackSlot.captured.onComplete()

        verify {
            coverArtCallbackMock wasNot Called
        }

        confirmVerified(coverArtCallbackMock)
    }

    @Test
    fun sgetCoverArtCallbackOnError_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<StringCallback>()
        every {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        metadataServiceClient.getCoverArtSubscribe(coverArtRequestData, coverArtCallbackMock)

        aidlCallbackSlot.captured.onError("error")

        verify {
            coverArtCallbackMock.onError("error")
        }

        confirmVerified(coverArtCallbackMock)
    }

    @Test
    fun getCoverArtCallbackOnError_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<StringCallback>()
        every {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        val requestId =
            metadataServiceClient.getCoverArtSubscribe(coverArtRequestData, coverArtCallbackMock)
        metadataServiceClient.getCoverArtDispose(requestId)

        aidlCallbackSlot.captured.onError("error")

        verify {
            coverArtCallbackMock wasNot Called
        }

        confirmVerified(coverArtCallbackMock)
    }

    @Test
    fun getCoverArtDispose_shouldFowardCallToAIDLInterfaceIfSubscribed() {
        val coverArtRequestData = CoverArtRequestData("artist", "title", "album")

        val requestId = metadataServiceClient.getCoverArtSubscribe(
            coverArtRequestData, coverArtCallbackMock
        )

        metadataServiceClient.getCoverArtDispose(requestId)

        verifySequence {
            metadataServiceAidlMock.getCoverArtSubscribe(
                CLIENT_ID, requestId, coverArtRequestData, any()
            )
            metadataServiceAidlMock.getCoverArtDispose(CLIENT_ID, requestId)
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun getCoverArtDispose_shouldDoNothingIfNotSubscribed() {
        metadataServiceClient.getCoverArtDispose(0)

        verify {
            metadataServiceAidlMock wasNot Called
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun getPermissionDataSubscribe_shouldForwardCallToAIDLInterface() {
        val requestId = metadataServiceClient.getPermissionDataSubscribe(permissionDataCallbackMock)

        verify {
            metadataServiceAidlMock.getPermissionDataSubscribe(CLIENT_ID, requestId, any())
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun getPermissionDataCallbackOnSuccess_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<PermissionDataSingleCallback>()
        every {
            metadataServiceAidlMock.getPermissionDataSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.getPermissionDataSubscribe(permissionDataCallbackMock)

        val permissionData = PermissionData(listOf(0))
        aidlCallbackSlot.captured.onSuccess(permissionData)

        verify {
            permissionDataCallbackMock.onSuccess(permissionData)
        }

        confirmVerified(permissionDataCallbackMock)
    }

    @Test
    fun getPermissionDataCallbackOnSuccess_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<PermissionDataSingleCallback>()
        every {
            metadataServiceAidlMock.getPermissionDataSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId = metadataServiceClient.getPermissionDataSubscribe(permissionDataCallbackMock)
        metadataServiceClient.getPermissionDataDispose(requestId)

        val permissionData = PermissionData(listOf(0))
        aidlCallbackSlot.captured.onSuccess(permissionData)

        verify {
            permissionDataCallbackMock wasNot Called
        }

        confirmVerified(permissionDataCallbackMock)
    }

    @Test
    fun getPermissionDataCallbackOnError_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<PermissionDataSingleCallback>()
        every {
            metadataServiceAidlMock.getPermissionDataSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.getPermissionDataSubscribe(permissionDataCallbackMock)

        aidlCallbackSlot.captured.onError("error")

        verify {
            permissionDataCallbackMock.onError("error")
        }

        confirmVerified(permissionDataCallbackMock)
    }

    @Test
    fun getPermissionDataCallbackOnError_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<PermissionDataSingleCallback>()
        every {
            metadataServiceAidlMock.getPermissionDataSubscribe(
                CLIENT_ID, any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId = metadataServiceClient.getPermissionDataSubscribe(permissionDataCallbackMock)
        metadataServiceClient.getPermissionDataDispose(requestId)

        aidlCallbackSlot.captured.onError("error")

        verify {
            permissionDataCallbackMock wasNot Called
        }

        confirmVerified(permissionDataCallbackMock)
    }

    @Test
    fun getPermissionDataDispose_shouldFowardCallToAIDLInterfaceIfSubscribed() {
        val requestId = metadataServiceClient.getPermissionDataSubscribe(permissionDataCallbackMock)

        metadataServiceClient.getPermissionDataDispose(requestId)

        verifySequence {
            metadataServiceAidlMock.getPermissionDataSubscribe(CLIENT_ID, requestId, any())
            metadataServiceAidlMock.getPermissionDataDispose(CLIENT_ID, requestId)
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun getPermissionDataDispose_shouldDoNothingIfNotSubscribed() {
        metadataServiceClient.changeOnlineLookupStateDispose(0)

        verify {
            metadataServiceAidlMock wasNot Called
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun observeSongLyricsSubscribe_shouldForwardCallToAIDLInterface() {
        val requestId = metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )

        verify {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, requestId, any(), any(), any()
            )
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun songLyricsCallbackOnNext_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<SongLyricsCallback>()
        every {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, any(), any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )

        val songLyrics = SongLyrics(
            "lyrics",
            1234
        )
        aidlCallbackSlot.captured.onNext(songLyrics)

        verify {
            songLyricsCallbackMock.onNext(songLyrics)
        }

        confirmVerified(songLyricsCallbackMock)
    }

    @Test
    fun songLyricsCallbackOnNext_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<SongLyricsCallback>()
        every {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, any(), any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId = metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )
        metadataServiceClient.observeSongLyricsDispose(requestId)

        val songLyrics = SongLyrics(
            "lyrics",
            1234
        )
        aidlCallbackSlot.captured.onNext(songLyrics)

        verify {
            songLyricsCallbackMock wasNot Called
        }

        confirmVerified(songLyricsCallbackMock)
    }

    @Test
    fun songLyricsCallbackOnComplete_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<SongLyricsCallback>()
        every {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, any(), any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )

        aidlCallbackSlot.captured.onComplete()

        verify {
            songLyricsCallbackMock.onComplete()
        }

        confirmVerified(songLyricsCallbackMock)
    }

    @Test
    fun songLyricsCallbackOnComplete_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<SongLyricsCallback>()
        every {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, any(), any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId = metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )
        metadataServiceClient.observeSongLyricsDispose(requestId)

        aidlCallbackSlot.captured.onComplete()

        verify {
            songLyricsCallbackMock wasNot Called
        }

        confirmVerified(songLyricsCallbackMock)
    }

    @Test
    fun songLyricsCallbackOnError_shouldForwardCallFromAIDLInterfaceIfSubscribed() {
        val aidlCallbackSlot = slot<SongLyricsCallback>()
        every {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, any(), any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )

        aidlCallbackSlot.captured.onError("error")

        verify {
            songLyricsCallbackMock.onError("error")
        }

        confirmVerified(songLyricsCallbackMock)
    }

    @Test
    fun songLyricsCallbackOnError_shouldDoNothingIfNotSubscribed() {
        val aidlCallbackSlot = slot<SongLyricsCallback>()
        every {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, any(), any(), any(), capture(aidlCallbackSlot)
            )
        } just Runs

        val requestId = metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )
        metadataServiceClient.observeSongLyricsDispose(requestId)

        aidlCallbackSlot.captured.onError("error")

        verify {
            songLyricsCallbackMock wasNot Called
        }

        confirmVerified(songLyricsCallbackMock)
    }

    @Test
    fun observeSongLyricsDispose_shouldForwardCallToAIDLInterfaceIfSubscribed() {
        val requestId = metadataServiceClient.observeSongLyricsSubscribe(
            SongLyrics.LYRICS_TYPE_FULL,
            false,
            songLyricsCallbackMock
        )

        metadataServiceClient.observeSongLyricsDispose(requestId)

        verifySequence {
            metadataServiceAidlMock.observeSongLyricsSubscribe(
                CLIENT_ID, requestId, any(), any(), any()
            )
            metadataServiceAidlMock.observeSongLyricsDispose(CLIENT_ID, requestId)
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @Test
    fun observeSongLyricsDispose_shouldDoNothingIfNotSubscribed() {
        metadataServiceClient.observeSongLyricsDispose(0)

        verify {
            metadataServiceAidlMock wasNot Called
        }

        confirmVerified(metadataServiceAidlMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }
}
