package technology.cariad.entertainment.metadataservice.data.provider.songinfo

import technology.cariad.entertainment.metadataservice.common.io.IFileDescriptor
import technology.cariad.entertainment.metadataservice.common.io.IInputStream
import technology.cariad.entertainment.metadataservice.common.io.IOutputStream
import technology.cariad.entertainment.metadataservice.common.io.IPipe
import technology.cariad.entertainment.metadataservice.common.io.IPipeFactory
import technology.cariad.entertainment.metadataservice.common.network.NetworkInfo
import technology.cariad.entertainment.metadataservice.data.provider.GnServiceRequestNr
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.ExternalId
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.GnRecognitionStatus
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.IGracenoteService
import technology.cariad.entertainment.metadataservice.data.provider.gracenote.SongRecognitionResult
import technology.cariad.entertainment.metadataservice.data.provider.songinfo.audiorecorder.IAudioRecorderThread
import technology.cariad.entertainment.metadataservice.domain.entity.ExternalIdentifierEntity
import technology.cariad.entertainment.metadataservice.domain.entity.ImageEntity
import technology.cariad.entertainment.metadataservice.domain.entity.SongInfoEntity
import technology.cariad.entertainment.metadataservice.domain.value.ServiceIdentifier
import technology.cariad.entertainment.metadataservice.mock.common.log.LoggerMock
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SongInfoProviderTest {
    private val graceNoteServiceMock = mockk<IGracenoteService>()
    private val audioRecorderThreadMock = mockk<IAudioRecorderThread>()

    private val pipeMock = mockk<IPipe>()
    private val readFdMock = mockk<IFileDescriptor>()
    private val writeFdMock = mockk<IFileDescriptor>()
    private val inputStreamMock = mockk<IInputStream>()
    private val outputStreamMock = mockk<IOutputStream>()

    private lateinit var songInfoProvider: SongInfoProvider

    @Before
    fun setup() {

        LoggerMock()

        every { graceNoteServiceMock.registerCallback(any()) } just Runs

        mockkObject(IPipeFactory)
        every { IPipeFactory.createPipe() } returns pipeMock

        every { pipeMock.readFd } returns readFdMock
        every { pipeMock.writeFd } returns writeFdMock
        every { pipeMock.readStream } returns inputStreamMock
        every { pipeMock.writeStream } returns outputStreamMock
        every {outputStreamMock.close()} just Runs

        every { readFdMock.detach() } returns 1

        songInfoProvider = SongInfoProvider(
            ServiceIdentifier.GRACENOTE_V1,
            graceNoteServiceMock,
            audioRecorderThreadMock
        )

        songInfoProvider.serviceId
    }

    @Test
    fun getSongInfo_shouldStartRecognitionOnFirstSubscription() = runBlocking {

        var cont: Continuation<Unit>? = null

        every { graceNoteServiceMock.startSongRecognition(any()) } answers {
            songInfoProvider.onStatus(GnRecognitionStatus.RUNNING)
        }

        every { audioRecorderThreadMock.start(any()) } answers {
            songInfoProvider.onRecognized(SongRecognitionResult())
            songInfoProvider.onRecognized(
                SongRecognitionResult(
                    image = byteArrayOf(),
                    externalIds = listOf(ExternalId("apple_music", "id"))
                )
            )
            songInfoProvider.onRecognized(
                SongRecognitionResult(
                    image = byteArrayOf(
                        0x12,
                        0x34
                    )
                )
            )
        }

        every { audioRecorderThreadMock.stop() } just Runs
        every { graceNoteServiceMock.stopSongRecognition() } answers {
            cont!!.resume(Unit)
        }

        val resultFlow = songInfoProvider.getSongInfo()

        val job: Job

        suspendCoroutine<Unit> {
            cont = it
            job = launch { resultFlow.first() }
        }
        job.join()


        verifyOrder {
            graceNoteServiceMock.startSongRecognition(any())
            audioRecorderThreadMock.start(any())
        }
    }

    @Test
    fun getSongInfo_shouldStopRecognitionAfterLastUnsubscribe() = runBlocking {

        var cont: Continuation<Unit>? = null

        every { graceNoteServiceMock.startSongRecognition(any()) } answers {
            songInfoProvider.onStatus(GnRecognitionStatus.RUNNING)
        }
        every { audioRecorderThreadMock.start(any()) } answers {
            songInfoProvider.onRecognized(SongRecognitionResult())
        }
        every { audioRecorderThreadMock.stop() } just Runs
        every { graceNoteServiceMock.stopSongRecognition() } answers {
            cont!!.resume(Unit)
        }

        var job: Job

        suspendCoroutine<Unit> {
            cont = it
            job = launch {
                songInfoProvider.getSongInfo().first()
            }
        }

        job.join()

        verifyOrder {
            audioRecorderThreadMock.stop()
            graceNoteServiceMock.stopSongRecognition()
        }
    }

    fun getRecognitionState(provider: SongInfoProvider): String {
        val flowField = SongInfoProvider::class.java.getDeclaredField("recognitionStatusFlow")
        flowField.isAccessible = true
        val flow = flowField.get(provider) as kotlinx.coroutines.flow.StateFlow<*>
        return flow.value.toString()
    }

    @Test
    fun onStatus_shouldUpdateRecognitionStateCorrectly() {
        val states = mapOf(
            GnRecognitionStatus.UNKNOWN to "ERROR",
            GnRecognitionStatus.STOPPED to "INIT",
            GnRecognitionStatus.RUNNING to "READY",
            GnRecognitionStatus.ERROR to "ERROR"
        )

        states.forEach { (inputStatus, expectedState) ->
            songInfoProvider.onStatus(inputStatus)
            val currentState = getRecognitionState(songInfoProvider)
            assert(currentState == expectedState) {
                "Expected state $expectedState but was $currentState"
            }
        }
    }

    @Test
    fun stopAudioRecording_shouldHandlePipeNullAndNotNull() {
        every { audioRecorderThreadMock.stop() } just Runs

        val pipeField = SongInfoProvider::class.java.getDeclaredField("pipe")
        pipeField.isAccessible = true

        val stopAudioRecordingMethod = SongInfoProvider::class.java.getDeclaredMethod("stopAudioRecording")
        stopAudioRecordingMethod.isAccessible = true

        // Pipe null case
        pipeField.set(songInfoProvider, null)
        stopAudioRecordingMethod.invoke(songInfoProvider)

        // Pipe non-null case
        pipeField.set(songInfoProvider, pipeMock)
        stopAudioRecordingMethod.invoke(songInfoProvider)

        verify { audioRecorderThreadMock.stop() }
        verify { outputStreamMock.close() }
    }

    @Test
    fun onRecognized_shouldEmitCorrectSongInfoAndImage() = runBlocking {
        every { graceNoteServiceMock.startSongRecognition(any()) } answers {
            songInfoProvider.onStatus(GnRecognitionStatus.RUNNING)
        }

        val result = SongRecognitionResult(
            title = "Title",
            album = "Album",
            artist = "Artist",
            year = "2025",
            genre = "Pop",
            mood = "Happy",
            tempo = "120",
            externalIds = listOf(ExternalId("spotify", "123"), ExternalId("unknown", "abc")),
            externalAlbumIds = listOf(ExternalId("apple_music", "abc")),
            image = byteArrayOf(1, 2, 3),
            gracenoteId = "gnId",
            gracenoteUniqueId = "gnUniqueId"
        )
        every { audioRecorderThreadMock.start(any()) } answers {
            songInfoProvider.onRecognized(result)
        }
        val emittedPair = mutableListOf<Pair<SongInfoEntity, ImageEntity>>()
        val job = launch {
            songInfoProvider.getSongInfo().collect {
                emittedPair.add(it)
                cancel()
            }
        }

        songInfoProvider.onRecognized(result)
        job.join()

        val songInfo = emittedPair.first().first
        val image = emittedPair.first().second

        assert(songInfo.title == "Title")
        assert(songInfo.externalIds.any { it.originType == ExternalIdentifierEntity.Companion.OriginType.SPOTIFY})
        assert(songInfo.externalAlbumIds.any { it.originType == ExternalIdentifierEntity.Companion.OriginType.APPLE})
        assert(image.imageData?.size == 3)
    }

    @Test
    fun selectNetwork_shouldUpdateApnConfig() {
        val networkInfoMock = mockk<NetworkInfo>()
        every { networkInfoMock.interfaceName } returns "eth0"
        every { graceNoteServiceMock.updateApnConfigEntry(any(), any()) } just Runs

        songInfoProvider.selectNetwork(networkInfoMock)
        verify { graceNoteServiceMock.updateApnConfigEntry(GnServiceRequestNr.SONGRECOGNITION, "eth0") }

        songInfoProvider.selectNetwork(null)
        verify { graceNoteServiceMock.updateApnConfigEntry(GnServiceRequestNr.SONGRECOGNITION, null) }
    }

    @Test
    fun onRecognized_shouldMapExternalAlbumIdsOriginTypeCorrectly() = runBlocking {
        every { graceNoteServiceMock.startSongRecognition(any()) } answers {
            songInfoProvider.onStatus(GnRecognitionStatus.RUNNING)
        }

        val externalAlbumIds = listOf(
            ExternalId("apple_music", "album_apple"),
            ExternalId("amazon_music", "album_amazon"),
            ExternalId("spotify_music", "album_spotify"),
            ExternalId("unknown_source", "album_unknown")
        )

        val result = SongRecognitionResult(
            externalAlbumIds = externalAlbumIds
        )

        every { audioRecorderThreadMock.start(any()) } answers {
            songInfoProvider.onRecognized(result)
        }

        val emittedResults = mutableListOf<Pair<SongInfoEntity, ImageEntity>>()
        val job = launch {
            songInfoProvider.getSongInfo().collect {
                emittedResults.add(it)
                cancel() // cancel after first emission to avoid infinite collection
            }
        }

        songInfoProvider.onRecognized(result)
        job.join()

        val externalAlbumIdsMapped = emittedResults.first().first.externalAlbumIds

        assert(externalAlbumIdsMapped.any { it.originType == ExternalIdentifierEntity.Companion.OriginType.APPLE })
        assert(externalAlbumIdsMapped.any { it.originType == ExternalIdentifierEntity.Companion.OriginType.AMAZON })
        assert(externalAlbumIdsMapped.any { it.originType == ExternalIdentifierEntity.Companion.OriginType.SPOTIFY })

        // The unknown source should not be mapped, so it should NOT be in externalAlbumIdsMapped
        assert(externalAlbumIdsMapped.none { it.id == "album_unknown" })
    }


    @After
    fun tearDown() {
        unmockkAll()
    }
}

