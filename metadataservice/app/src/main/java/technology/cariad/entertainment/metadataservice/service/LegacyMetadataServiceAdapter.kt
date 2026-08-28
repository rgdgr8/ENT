package technology.cariad.entertainment.metadataservice.service

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import technology.cariad.entertainment.metadataservice.BuildConfig
import technology.cariad.entertainment.metadataservice.MetadataServiceApp
import de.esolutions.metadataserviceclient.api.IMetadataServiceAidl as LegacyService
import de.esolutions.metadataserviceclient.api.IMetadataServiceManagement as LegacyManagement

/**
 * Serves clients compiled against the pre-CARiAD AIDL namespace.
 *
 * An AIDL interface name is part of Binder's wire contract.  Merely adding the old intent action
 * therefore cannot support an old client: its first transaction is rejected when the binder
 * advertises the new interface descriptor.  This adapter owns the legacy descriptor and forwards
 * calls to the CARiAD implementation, translating the package-renamed parcelables and callbacks.
 */
class LegacyMetadataServiceAdapter(private val delegate: MetadataServiceImpl) : LegacyService.Stub() {

    private val management = object : LegacyManagement.Stub() {
        override fun registerClient(clientName: String, deathRecipient: IBinder): Int =
            delegate.management().registerClient(clientName, deathRecipient)

        override fun unregisterClient(clientId: Int, clientName: String) {
            delegate.management().unregisterClient(clientId, clientName)
        }

        override fun registerClientWithIds(
            clientName: String,
            deathRecipient: IBinder,
            clientId: Int,
            requestId: Int
        ): Int = delegate.management().registerClientWithIds(clientName, deathRecipient, clientId, requestId)
    }

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (code in IBinder.FIRST_CALL_TRANSACTION..IBinder.LAST_CALL_TRANSACTION &&
            !ClientPermission.hasUseServicePermission(MetadataServiceApp.app)
        ) {
            throw SecurityException("caller does not hold USE_SERVICE permission")
        }
        return super.onTransact(code, data, reply, flags)
    }

    override fun management(): LegacyManagement = management

    override fun changeOnlineLookupStateSubscribe(
        clientId: Int, requestId: Int, enabled: Boolean,
        callback: de.esolutions.metadataserviceclient.api.BooleanSingleCallback
    ) = delegate.changeOnlineLookupStateSubscribe(clientId, requestId, enabled,
        object : technology.cariad.entertainment.metadataserviceclient.api.BooleanSingleCallback.Stub() {
            override fun onSuccess(bool: Boolean) = callback.onSuccess(bool)
            override fun onError(message: String?) = callback.onError(message)
        })

    override fun changeOnlineLookupStateDispose(clientId: Int, requestId: Int) =
        delegate.changeOnlineLookupStateDispose(clientId, requestId)

    override fun observeRadioStationDataSubscribe(
        clientId: Int, requestId: Int, bearer: de.esolutions.metadataserviceclient.api.StationBearer,
        cachedLastModified: String?, callback: de.esolutions.metadataserviceclient.api.StationCallback
    ) = delegate.observeRadioStationDataSubscribe(
        clientId, requestId, bearer.toCurrent(), requireNotNull(cachedLastModified),
        object : technology.cariad.entertainment.metadataserviceclient.api.StationCallback.Stub() {
            override fun onNext(station: technology.cariad.entertainment.metadataserviceclient.api.Station?) {
                station?.let { callback.onNext(it.toLegacy()) }
            }
            override fun onComplete() = callback.onComplete()
            override fun onError(message: String?) = callback.onError(message)
        })

    override fun observeRadioStationDataDispose(clientId: Int, requestId: Int) =
        delegate.observeRadioStationDataDispose(clientId, requestId)

    override fun observeSongRecognitionSubscribe(
        clientId: Int, requestId: Int,
        callback: de.esolutions.metadataserviceclient.api.RecognizedSongInfoCallback
    ) = delegate.observeSongRecognitionSubscribe(clientId, requestId,
        object : technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfoCallback.Stub() {
            override fun onNext(info: technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo?) {
                info?.let { callback.onNext(it.toLegacy()) }
            }
            override fun onComplete() = callback.onComplete()
            override fun onError(message: String?) = callback.onError(message)
        })

    override fun observeSongRecognitionDispose(clientId: Int, requestId: Int) =
        delegate.observeSongRecognitionDispose(clientId, requestId)

    override fun getCoverArtSubscribe(
        clientId: Int, requestId: Int, requestData: de.esolutions.metadataserviceclient.api.CoverArtRequestData,
        callback: de.esolutions.metadataserviceclient.api.StringCallback
    ) = delegate.getCoverArtSubscribe(clientId, requestId, requestData.toCurrent(),
        object : technology.cariad.entertainment.metadataserviceclient.api.StringCallback.Stub() {
            override fun onNext(value: String?) = callback.onNext(value?.toLegacyImageUri())
            override fun onComplete() = callback.onComplete()
            override fun onError(message: String?) = callback.onError(message)
        })

    override fun getCoverArtDispose(clientId: Int, requestId: Int) =
        delegate.getCoverArtDispose(clientId, requestId)

    override fun getPermissionDataSubscribe(
        clientId: Int, requestId: Int,
        callback: de.esolutions.metadataserviceclient.api.PermissionDataSingleCallback
    ) = delegate.getPermissionDataSubscribe(clientId, requestId,
        object : technology.cariad.entertainment.metadataserviceclient.api.PermissionDataSingleCallback.Stub() {
            override fun onSuccess(data: technology.cariad.entertainment.metadataserviceclient.api.PermissionData?) {
                data?.let { callback.onSuccess(de.esolutions.metadataserviceclient.api.PermissionData(it.missingPermissions)) }
            }
            override fun onError(message: String?) = callback.onError(message)
        })

    override fun getPermissionDataDispose(clientId: Int, requestId: Int) =
        delegate.getPermissionDataDispose(clientId, requestId)

    override fun observeRadioStationDataAsiaSubscribe(
        clientId: Int, requestId: Int, bearer: de.esolutions.metadataserviceclient.api.StationBearerAsia,
        callback: de.esolutions.metadataserviceclient.api.StationAsiaSingleCallback
    ) = delegate.observeRadioStationDataAsiaSubscribe(clientId, requestId, bearer.toCurrent(),
        object : technology.cariad.entertainment.metadataserviceclient.api.StationAsiaSingleCallback.Stub() {
            override fun onSuccess(data: technology.cariad.entertainment.metadataserviceclient.api.StationAsia?) {
                data?.let { callback.onSuccess(it.toLegacy()) }
            }
            override fun onError(message: String?) = callback.onError(message)
        })

    override fun observeRadioStationDataAsiaDispose(clientId: Int, requestId: Int) =
        delegate.observeRadioStationDataAsiaDispose(clientId, requestId)

    override fun observeSongLyricsSubscribe(
        clientId: Int, requestId: Int, type: Int, familyFriendly: Boolean,
        callback: de.esolutions.metadataserviceclient.api.SongLyricsCallback
    ) = delegate.observeSongLyricsSubscribe(clientId, requestId, type, familyFriendly,
        object : technology.cariad.entertainment.metadataserviceclient.api.SongLyricsCallback.Stub() {
            override fun onNext(data: technology.cariad.entertainment.metadataserviceclient.api.SongLyrics?) {
                data?.let { callback.onNext(de.esolutions.metadataserviceclient.api.SongLyrics(it.lyricsJson, it.playPositionMs)) }
            }
            override fun onComplete() = callback.onComplete()
            override fun onError(message: String?) = callback.onError(message)
        })

    override fun observeSongLyricsDispose(clientId: Int, requestId: Int) =
        delegate.observeSongLyricsDispose(clientId, requestId)

    override fun getArtistMetadataSubscribe(clientId: Int, requestId: Int, artistId: String?, callback: de.esolutions.metadataserviceclient.api.MusicMetadataSingleCallback) =
        delegate.getArtistMetadataSubscribe(clientId, requestId, requireNotNull(artistId), callback.toCurrent())

    override fun getArtistMetadataDispose(clientId: Int, requestId: Int) = delegate.getArtistMetadataDispose(clientId, requestId)

    override fun getAlbumMetadataSubscribe(clientId: Int, requestId: Int, albumId: String?, callback: de.esolutions.metadataserviceclient.api.MusicMetadataSingleCallback) =
        delegate.getAlbumMetadataSubscribe(clientId, requestId, requireNotNull(albumId), callback.toCurrent())

    override fun getAlbumMetadataDispose(clientId: Int, requestId: Int) = delegate.getAlbumMetadataDispose(clientId, requestId)

    override fun getRecordingMetadataSubscribe(clientId: Int, requestId: Int, recordingId: String?, callback: de.esolutions.metadataserviceclient.api.MusicMetadataSingleCallback) =
        delegate.getRecordingMetadataSubscribe(clientId, requestId, requireNotNull(recordingId), callback.toCurrent())

    override fun getRecordingMetadataDispose(clientId: Int, requestId: Int) = delegate.getRecordingMetadataDispose(clientId, requestId)

    override fun getRecordingMetadataByNameSubscribe(clientId: Int, requestId: Int, recordingName: String?, artistName: String?, callback: de.esolutions.metadataserviceclient.api.MusicMetadataSingleCallback) =
        delegate.getRecordingMetadataByNameSubscribe(clientId, requestId, requireNotNull(recordingName), requireNotNull(artistName), callback.toCurrent())

    override fun getRecordingMetadataByNameDispose(clientId: Int, requestId: Int) = delegate.getRecordingMetadataByNameDispose(clientId, requestId)
}

private fun de.esolutions.metadataserviceclient.api.StationBearer.toCurrent() =
    technology.cariad.entertainment.metadataserviceclient.api.StationBearer(type, pi, frequencyKhz, sId, eId, scIds, ecc, tx, cc, multicastId, mcc)

private fun de.esolutions.metadataserviceclient.api.StationBearerAsia.toCurrent() =
    technology.cariad.entertainment.metadataserviceclient.api.StationBearerAsia(type, frequencyKhz, mcc)

private fun de.esolutions.metadataserviceclient.api.CoverArtRequestData.toCurrent() =
    technology.cariad.entertainment.metadataserviceclient.api.CoverArtRequestData(artist, title, album)

private fun technology.cariad.entertainment.metadataserviceclient.api.Station.toLegacy() =
    de.esolutions.metadataserviceclient.api.Station(
        metadata?.let { de.esolutions.metadataserviceclient.api.StationMetadata(it.shortName, it.mediumName, it.longName, it.description, it.genres) },
        logo?.let { de.esolutions.metadataserviceclient.api.StationLogo(it.uri.toLegacyImageUri()) },
        logoSmall?.let { de.esolutions.metadataserviceclient.api.StationLogo(it.uri.toLegacyImageUri()) },
        streams.map { de.esolutions.metadataserviceclient.api.StationStream(it.url, it.offsetMillis, it.quality) },
        bearers.map { de.esolutions.metadataserviceclient.api.StationBearer(it.type, it.pi, it.frequencyKhz, it.sId, it.eId, it.scIds, it.ecc, it.tx, it.cc, it.multicastId, it.mcc) },
        lastModified,
        providerIdentifiers.map { de.esolutions.metadataserviceclient.api.ProviderIdentifier(it.originType, it.id) }
    )

private fun technology.cariad.entertainment.metadataserviceclient.api.StationAsia.toLegacy() =
    de.esolutions.metadataserviceclient.api.StationAsia(
        name,
        logo?.let { de.esolutions.metadataserviceclient.api.StationLogo(it.uri.toLegacyImageUri()) },
        providerIdentifier?.let { de.esolutions.metadataserviceclient.api.ProviderIdentifier(it.originType, it.id) }
    )

private fun technology.cariad.entertainment.metadataserviceclient.api.RecognizedSongInfo.toLegacy() =
    de.esolutions.metadataserviceclient.api.RecognizedSongInfo(
        artist, title, album, year, coverartUri?.toLegacyImageUri(), era, genre, mood, tempo,
        externalIds.map { de.esolutions.metadataserviceclient.api.ExternalIdentifier(it.originType, it.id) },
        gracenoteId, gracenoteUniqueId,
        externalAlbumIds.map { de.esolutions.metadataserviceclient.api.ExternalIdentifier(it.originType, it.id) }
    )

private fun de.esolutions.metadataserviceclient.api.MusicMetadataSingleCallback.toCurrent() =
    object : technology.cariad.entertainment.metadataserviceclient.api.MusicMetadataSingleCallback.Stub() {
        override fun onSuccess(data: technology.cariad.entertainment.metadataserviceclient.api.MusicMetadata?) {
            data?.let {
                this@toCurrent.onSuccess(de.esolutions.metadataserviceclient.api.MusicMetadata(
                    it.content,
                    it.error?.let { error -> de.esolutions.metadataserviceclient.api.MetadataError(error.errorType, error.statusCode, error.errorMessage) }
                ))
            }
        }
        override fun onError(message: String?) = this@toCurrent.onError(message)
    }

private fun String.toLegacyImageUri(): String =
    replace(BuildConfig.IMAGE_PROVIDER_AUTHORITY, BuildConfig.LEGACY_IMAGE_PROVIDER_AUTHORITY)
