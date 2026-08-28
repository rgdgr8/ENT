package technology.cariad.entertainment.metadataserviceclient.api

import android.content.Context
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ServiceTestRule
import technology.cariad.entertainment.metadataserviceclient.util.DabBearer
import technology.cariad.entertainment.metadataserviceclient.util.FmRdsBearer
import technology.cariad.entertainment.metadataserviceclient.util.IbocBearer
import technology.cariad.entertainment.metadataserviceclient.util.TypedBearer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetadataServiceValuePassingInstrumentedTest {

    @get:Rule
    val serviceRule = ServiceTestRule()

    lateinit var instrumentationContext: Context

    private lateinit var remoteTestee: IMetadataService

    @Before
    fun setUp() {
        val bindIntent = Intent("MetadataInstrumentedTestService").apply {
            this.setPackage("technology.cariad.entertainment.metadataserviceclient.test")
        }

        instrumentationContext = InstrumentationRegistry.getInstrumentation().context

        this.remoteTestee = IMetadataServiceClientAdapter.fromBinder(
            this.serviceRule.bindService(bindIntent),
            instrumentationContext
        )!!
        assertThat(this.remoteTestee).isNotNull
    }

    @Test
    fun changeOnlineLookupState_ShouldPassValuesCorrectly() {
        val callTestObsTrue = this.remoteTestee.changeOnlineLookupState(true).test()
        callTestObsTrue.awaitCount(1)
        callTestObsTrue.assertValue(true)

        val callTestObsFalse = this.remoteTestee.changeOnlineLookupState(false).test()
        callTestObsFalse.awaitCount(1)
        callTestObsFalse.assertValue(false)
    }

    @Test
    fun observeRadioStationData_ShouldPassUnkownStationDataValuesCorrectly() {
        val inputTypedBearer = IbocBearer("tx", "cc", "mulitcastdId")
        val callTestObs =
            this.remoteTestee.observeRadioStationData(inputTypedBearer.rawBearer, "").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValueCount(0).assertComplete();
    }

    @Test
    fun observeRadioStationData_ShouldPassIllegalStationDataValuesCorrectly() {
        val illegalDabBearer = StationBearer(StationBearer.TYPE_DAB)
        val callTestObs = this.remoteTestee.observeRadioStationData(illegalDabBearer, "").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValueCount(0)
        callTestObs.assertError(RuntimeException::class.java) // Generated code extracts message from remove exception, morphs to RuntimeException
        callTestObs.assertError { it.message == "sId was null!" }
    }

    @Test
    fun observeRadioStationData_ShouldPassMinimalStationDataValuesCorrectly() {
        val inputTypedBearer = FmRdsBearer("pi", "freq", null, "mcc")
        val callTestObs =
            this.remoteTestee.observeRadioStationData(inputTypedBearer.rawBearer, "").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValueCount(1).assertComplete();
        assertThat(callTestObs.values()[0].metadata).isNull()
        assertThat(callTestObs.values()[0].logo).isNull()
        assertThat(callTestObs.values()[0].logoSmall).isNull()
        assertThat(callTestObs.values()[0].streams).isEmpty()
        assertThat(callTestObs.values()[0].bearers).isEmpty()
        assertThat(callTestObs.values()[0].lastModified).isEqualTo("gutenTag")
    }

    @Test
    fun observeRadioStationData_ShouldPassMaximumStationDataValuesCorrectly() {
        val inputTypedBearer = DabBearer("sid", "eId", "scIds", "ecc", "mcc")
        val callTestObs =
            this.remoteTestee.observeRadioStationData(inputTypedBearer.rawBearer, "").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValueCount(1)
        with(callTestObs.values()[0]) {
            assertThat(this.metadata).isEqualTo(
                StationMetadata(
                    "short",
                    "medium",
                    "long",
                    "desc",
                    listOf("genre")
                )
            )
            assertThat(this.logo).isEqualTo(StationLogo("uri_default"))
            assertThat(this.logoSmall).isEqualTo(StationLogo("uri_small"))

            with(this.streams) {
                assertThat(this).hasSize(2)
                assertThat(this[0]).isEqualTo(
                    StationStream(
                        "url_low",
                        null,
                        null
                    )
                )
                assertThat(this[1]).isEqualTo(
                    StationStream(
                        "url_high",
                        100,
                        StationStream.QUALITY_HIGH
                    )
                )
            }

            with(this.bearers) {
                assertThat(this).hasSize(4)
                assertThat(this[0].toTyped()).isEqualTo(inputTypedBearer)
                assertThat(this[1].toTyped()).isEqualTo(FmRdsBearer("pi", "freq", "ecc", "mcc"))
                assertThat(this[2].toTyped()).isEqualTo(FmRdsBearer("pi", "freq", null, "mcc"))
                assertThat(this[3].toTyped()).isEqualTo(IbocBearer("tx", "cc", "multicastId"))
            }

            assertThat(this.lastModified).isEqualTo("gutenTag")
        }
    }

    @Test
    fun observeSongRecognition_ShouldPassValuesCorrectly() {
        val callTestObs = this.remoteTestee.observeSongRecognition().test()

        callTestObs.awaitCount(2)
        callTestObs.assertValueCount(2)

        with(callTestObs.values()[0]) {
            assertThat(this).isEqualTo(
                RecognizedSongInfo(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    emptyList(),
                    null,
                    null,
                    emptyList(),
                )
            )
        }
        with(callTestObs.values()[1]) {
            assertThat(this.artist).isEqualTo("artist")
            assertThat(this.title).isEqualTo("title")
            assertThat(this.album).isEqualTo("album")
            assertThat(this.year).isEqualTo("year")
            assertThat(this.coverartUri).isEqualTo("uri")
            assertThat(this.era).isEqualTo("era")
            assertThat(this.genre).isEqualTo("genre")
            assertThat(this.mood).isEqualTo("mood")
            assertThat(this.tempo).isEqualTo("tempo")

            with(this.externalIds) {
                assertThat(this).hasSize(3)
                assertThat(this[0]).isEqualTo(
                    ExternalIdentifier(
                        ExternalIdentifier.ORIGIN_TYPE_AMAZON,
                        "id_amazon"
                    )
                )
                assertThat(this[1]).isEqualTo(
                    ExternalIdentifier(
                        ExternalIdentifier.ORIGIN_TYPE_APPLE,
                        "id_apple"
                    )
                )
                assertThat(this[2]).isEqualTo(
                    ExternalIdentifier(
                        ExternalIdentifier.ORIGIN_TYPE_SPOTIFY,
                        "id_spotify"
                    )
                )
            }

            assertThat(this.gracenoteId).isEqualTo("gracenoteId")
            assertThat(this.gracenoteUniqueId).isEqualTo("gracenoteUniqueId")

            with(this.externalAlbumIds) {
                assertThat(this).hasSize(3)
                assertThat(this[0]).isEqualTo(
                    ExternalIdentifier(
                        ExternalIdentifier.ORIGIN_TYPE_AMAZON,
                        "album_id_amazon"
                    )
                )
                assertThat(this[1]).isEqualTo(
                    ExternalIdentifier(
                        ExternalIdentifier.ORIGIN_TYPE_APPLE,
                        "album_id_apple"
                    )
                )
                assertThat(this[2]).isEqualTo(
                    ExternalIdentifier(
                        ExternalIdentifier.ORIGIN_TYPE_SPOTIFY,
                        "album_id_spotify"
                    )
                )
            }
        }
    }

    @Test
    fun getCoverArt_ShouldPassValuesCorrectly() {
        val requestData = CoverArtRequestData("artist", "title", "album")
        val callTestObs = this.remoteTestee.getCoverArt(requestData).test()

        callTestObs.awaitCount(1)
        callTestObs.assertValueCount(1).assertValue("uri_for_$requestData")
    }

    @Test
    fun getPermissionData_ShouldPassValuesCorrectly() {
        val callTestObs = this.remoteTestee.getPermissionData().test()
        callTestObs.awaitCount(1)
        callTestObs.assertValue(PermissionData(listOf(1, 2, 3)))
    }

    @Test
    fun observeRadioStationDataAsia_ShouldPassIllegalStationDataValuesCorrectly() {
        val illegalBearer = StationBearerAsia(-1, "", "")
        val callTestObs = this.remoteTestee.observeRadioStationDataAsia(illegalBearer).test()

        callTestObs.awaitCount(1)
        // Generated code extracts message from remote exception, morphs to RuntimeException
        callTestObs.assertError(RuntimeException::class.java)
        callTestObs.assertError { it.message == "invalid bearer type" }
    }

    @Test
    fun observeRadioStationDataAsia_ShouldPassMinimalStationDataValuesCorrectly() {
        val inputBearer = StationBearerAsia(StationBearerAsia.TYPE_AM, "", "")
        val callTestObs =
            this.remoteTestee.observeRadioStationDataAsia(inputBearer).test()

        callTestObs.awaitCount(1)
        callTestObs.assertComplete()
        assertThat(callTestObs.values()[0].name).isNull()
        assertThat(callTestObs.values()[0].logo).isNull()
        assertThat(callTestObs.values()[0].providerIdentifier).isNull()
    }

    @Test
    fun observeRadioStationDataAsia_ShouldPassMaximumStationDataValuesCorrectly() {
        val inputBearer = StationBearerAsia(StationBearerAsia.TYPE_FM, "freq", "mcc")
        val callTestObs = this.remoteTestee.observeRadioStationDataAsia(inputBearer).test()

        callTestObs.awaitCount(1)
        callTestObs.assertComplete()
        with(callTestObs.values()[0]) {
            assertThat(this.name).isEqualTo("name")
            assertThat(this.logo).isEqualTo(StationLogo("uri_default"))
            assertThat(this.providerIdentifier).isEqualTo(ProviderIdentifier(3, "provider"))
        }
    }

    @Test
    fun observeSongLyrics_ShouldPassValuesCorrectly() {
        val callTestObs =
            this.remoteTestee.observeSongLyrics(SongLyrics.LYRICS_TYPE_FULL, false).test()

        callTestObs.awaitCount(2)
        callTestObs.assertValueCount(2)

        with(callTestObs.values()[0]) {
            assertThat(this).isEqualTo(
                SongLyrics(
                    null,
                    -1
                )
            )
        }
        with(callTestObs.values()[1]) {
            assertThat(this.lyricsJson).isEqualTo("lyrics")
            assertThat(this.playPositionMs).isEqualTo(1234)

        }
    }

    @Test
    fun getAlbumMetadata_ShouldPassValuesCorrectly() {
        val callTestObs = this.remoteTestee.getAlbumMetadata("albumId").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValue(
                MusicMetadata(
                    "album metadata",
                    MetadataError(
                        MetadataError.NONE,
                        null,
                        null
                    )
                )
            )
        }

    @Test
    fun getArtistMetadata_ShouldPassValuesCorrectly() {
        val callTestObs = this.remoteTestee.getArtistMetadata("artistId").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValue(
            MusicMetadata(
                "artist metadata",
                MetadataError(
                    MetadataError.NONE,
                    null,
                    null
                )
            )
        )
    }

    @Test
    fun getRecordingMetadata_ShouldPassValuesCorrectly() {
        val callTestObs = this.remoteTestee.getRecordingMetadata("recordingId").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValue(
            MusicMetadata(
                "recording metadata",
                MetadataError(
                    MetadataError.NONE,
                    null,
                    null
                )
            )
        )
    }

    @Test
    fun getRecordingMetadataByName_ShouldPassValuesCorrectly() {
        val callTestObs = this.remoteTestee.getRecordingMetadataByName("recordingName", "artistName").test()

        callTestObs.awaitCount(1)
        callTestObs.assertValue(
            MusicMetadata(
                "recording metadata",
                MetadataError(
                    MetadataError.NONE,
                    null,
                    null
                )
            )
        )
    }

    //
    //

    private fun StationBearer.toTyped() = TypedBearer.fromRawBearer(this)
}