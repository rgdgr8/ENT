package technology.cariad.entertainment.metadataserviceclient

import android.app.Service
import android.content.Intent
import android.os.IBinder
import technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData
import technology.cariad.entertainment.metadataserviceclient.api.ExternalIdentifier
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataService
import technology.cariad.entertainment.metadataserviceclient.api.IMetadataServiceAidlProviderAdapter
import technology.cariad.entertainment.metadataserviceclient.api.MusicMetadata
import technology.cariad.entertainment.metadataserviceclient.api.MetadataError
import technology.cariad.entertainment.metadataserviceclient.api.PermissionData
import technology.cariad.entertainment.metadataserviceclient.api.ProviderIdentifier
import technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo
import technology.cariad.entertainment.metadataserviceclient.api.SongLyrics
import technology.cariad.entertainment.metadataserviceclient.api.Station
import technology.cariad.entertainment.metadataserviceclient.api.StationAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationBearer
import technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia
import technology.cariad.entertainment.metadataserviceclient.api.StationLogo
import technology.cariad.entertainment.metadataserviceclient.api.StationMetadata
import technology.cariad.entertainment.metadataserviceclient.api.StationStream
import technology.cariad.entertainment.metadataserviceclient.util.FmRdsBearer
import technology.cariad.entertainment.metadataserviceclient.util.IbocBearer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class MetadataInstrumentedTestService : Service() {
    override fun onBind(intent: Intent?): IBinder =
        IMetadataServiceAidlProviderAdapter(MetadataServiceTestImpl(), this)

    private class MetadataServiceTestImpl : IMetadataService {
        override fun changeOnlineLookupState(enabled: Boolean): Single<Boolean> =
            Single.just(enabled)

        override fun observeRadioStationData(
            bearer: StationBearer,
            cachedLastModified: String
        ): Observable<Station> =
            when {
                bearer.type == StationBearer.TYPE_IBOC -> Observable.empty()
                bearer.type == StationBearer.TYPE_FM_RDS -> Observable.just(
                    Station(
                        null,
                        null,
                        null,
                        emptyList(),
                        emptyList(),
                        "gutenTag",
                        emptyList()
                    )
                )
                bearer.type == StationBearer.TYPE_DAB && bearer.sId.isNullOrBlank() -> Observable.error(
                    IllegalArgumentException("sId was null!")
                )
                else -> this.observeMaximalStation(bearer, cachedLastModified)
            }

        override fun observeSongRecognition(): Observable<RecognizedSongInfo> = Observable.just(
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
                emptyList()
            ),
            RecognizedSongInfo(
                "artist", "title", "album", "year", "uri", "era", "genre", "mood", "tempo",
                listOf(
                    ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_AMAZON, "id_amazon"),
                    ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_APPLE, "id_apple"),
                    ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_SPOTIFY, "id_spotify")
                ),
                "gracenoteId", "gracenoteUniqueId",
                listOf(
                    ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_AMAZON, "album_id_amazon"),
                    ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_APPLE, "album_id_apple"),
                    ExternalIdentifier(ExternalIdentifier.ORIGIN_TYPE_SPOTIFY, "album_id_spotify")
                ),
            )
        )

        override fun getCoverArt(requestData: CoverArtRequestData): Observable<String> =
            Observable.just("uri_for_$requestData")

        override fun getPermissionData(): Single<PermissionData> = Single.just(
            PermissionData(listOf(1, 2, 3))

        )

        override fun observeRadioStationDataAsia(bearer: StationBearerAsia): Single<StationAsia> =
            when (bearer.type) {
                StationBearerAsia.TYPE_AM -> Single.just(
                    StationAsia(
                        null,
                        null,
                        null
                    )
                )
                StationBearerAsia.TYPE_FM -> Single.just(
                    StationAsia(
                        "name",
                        StationLogo("uri_default"),
                        ProviderIdentifier(3, "provider"),
                    )
                )
                else -> Single.error(
                    IllegalArgumentException("invalid bearer type")
                )
            }

        override fun observeSongLyrics(type: Int, familyFriendly: Boolean): Observable<SongLyrics> =
            Observable.just(
                SongLyrics(
                    null,
                    -1,
                ),
                SongLyrics(
                    "lyrics", 1234
                )
            )

        override fun getArtistMetadata(artistId: String): Single<MusicMetadata> =
            Single.just(
                MusicMetadata(
                    "artist metadata",
                    MetadataError(
                        MetadataError.NONE,
                        null,
                        null
                    )
                )
            )

        override fun getAlbumMetadata(albumId: String): Single<MusicMetadata> =
            Single.just(
                MusicMetadata(
                    "album metadata",
                    MetadataError(
                        MetadataError.NONE,
                        null,
                        null
                    )
                )
            )

        override fun getRecordingMetadata(recordingId: String): Single<MusicMetadata> =
            Single.just(
                MusicMetadata(
                    "recording metadata",
                    MetadataError(
                        MetadataError.NONE,
                        null,
                        null
                    )
                )
            )

        override fun getRecordingMetadataByName(
            recordingName: String,
            artistName: String
        ): Single<MusicMetadata> =
            Single.just(
                MusicMetadata(
                    "recording metadata",
                    MetadataError(
                        MetadataError.NONE,
                        null,
                        null
                    )
                )
            )

        //
        //

        private fun observeMaximalStation(
            inBearer: StationBearer,
            @Suppress("UNUSED_PARAMETER") cachedLastModified: String?
        ) =
            Observable.just(
                Station(
                    StationMetadata("short", "medium", "long", "desc", listOf("genre")),
                    StationLogo("uri_default"),
                    StationLogo("uri_small"),
                    listOf(
                        StationStream("url_low", null, null),
                        StationStream("url_high", 100, StationStream.QUALITY_HIGH)
                    ),
                    listOf(
                        inBearer,
                        FmRdsBearer("pi", "freq", "ecc", "mcc").rawBearer,
                        FmRdsBearer("pi", "freq", null, "mcc").rawBearer,
                        IbocBearer("tx", "cc", "multicastId").rawBearer,
                    ),
                    "gutenTag",
                    listOf(
                        ProviderIdentifier(1, "provider_1"),
                        ProviderIdentifier(2, "provider_2")
                    )
                )
            )
    }
}