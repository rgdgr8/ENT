// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 03.06.2020.
//

#include <unistd.h>

#include <utility>
#include <fstream>

#include <gnsdk.hpp>

#include "../helper/GnServiceDataTypes.hxx"
#include "../helper/GnServiceLogging.hxx"
#include "../resultdb/GnLocalLookupDb.hxx"
#include "../event/GnServiceStatusEventHandler.hxx"

#include "GnRequestSongRecognition.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Request
DECLARE_CHANNEL_EXTERN();
#define LOG_TAG "GnRequestSongRecog"

using std::string;
using gnservice::GnLocalLookupDB;
using gracenote::radioid::GnRadioId;
using gracenote::radioid::GnRadioIdIdentifyingStatus;
using gracenote::radioid::GnRadioIdResultStatus;
using gracenote::GnUser;
using gracenote::GnStatus;
using gracenote::GnError;
using gracenote::GnImageSize;
using gracenote::GnImageSize::kImageSizeMedium;
using gracenote::GnLookupMode;
using gracenote::GnLookupMode::kLookupModeLocal;
using gracenote::GnLookupMode::kLookupModeOnlineNoCache;
using gracenote::GnAssetFetch;
using gracenote::IGnCancellable;
using gracenote::metadata::GnTrack;
using gracenote::metadata::GnResponseAlbums;
using gracenote::metadata::GnAlbum;
using gracenote::metadata::GnAsset;
using gracenote::metadata::kDataLevel_1;
using gracenote::metadata::kDataLevel_2;
using gracenote::metadata::kDataLevel_3;
using gracenote::metadata::kDataLevel_4;
using gracenote::kLookupDataContent;
using gracenote::kLookupDataSonicData;
using gracenote::kLookupDataPlaylist;

GnRequestSongRecognition::GnRequestSongRecognition(IGnSongRecognitionEventListener *recogListener,
                                                   GnUser *user,
                                                   gnservice::GnLocalLookupDB *resultDb,
                                                   string path) :
    GnRequest(user, resultDb),
    m_recogListener(recogListener),
    m_gnRadioId(*m_gnUser, this),
    m_path(std::move(path)),
    m_audioFd(0),
    m_audioFormat({48000, 16, 2}),
    m_trackInfo()
{
    setRequestName("REQUEST_SONGRECOGNITION");
}

void GnRequestSongRecognition::setAudioFormat(GnServiceAudioFormat audioFormat)
{
    m_audioFormat = audioFormat;
}

void GnRequestSongRecognition::setAudioFileDescriptor(int32_t audioFileDesc)
{
    m_audioFd = audioFileDesc;
}

void GnRequestSongRecognition::executeTask()
{
    USE_CHANNEL();
    ALOG_DEBUG("starting song recognition request task");
#ifdef GN_INTEGRATE_FRAMEWORK
    TBIRC::mmx::MetadataService::GnSongRecognitionStatus(TBIRC::mmx::MetadataService::EGnSongRecognition::GnSongRecognitionStarted);
#endif

    bool internalError = false;
    int8_t emptyPipeReadCount = 0;

    m_gnRadioId.Options().ResultSingle(true);
    m_gnRadioId.Options().PreferResultCoverart(true);
    m_gnRadioId.Options().LookupData(kLookupDataContent, true);
    m_gnRadioId.Options().LookupData(kLookupDataSonicData, true);
    m_gnRadioId.Options().LookupData(kLookupDataPlaylist, true);

    gracenote::dsp::GnDsp gnDsp = gracenote::dsp::GnDsp::Enable();
    gnDsp.AudioClassifierStorageLocation(m_path.c_str());
    ALOG_INFO_CV("trying to get classifier at '%s'", m_path.c_str());

    // TODO(miir1367): make readSize somehow configurable, in order to find optimum value
    size_t readSize = 1920;
    void* readBuffer = malloc(readSize);

    // check if an audio file descriptor is given
    if ( m_audioFd == 0)
    {
        ALOG_WARN("0 audio file descriptor was given");
    }

    if(m_audioFd < 0)
    {
        ALOG_ERR_CV("Invalid audio file descriptor given %d, cancelling request", m_audioFd);
        internalError = true;
        goto GOTO_END;
    }

    if ( readBuffer == nullptr )
    {
        ALOG_ERR("could not allocate read buffer, cancelling request");
        internalError = true;
        goto GOTO_END;
    }

    try
    {
        m_gnRadioId.AudioProcessStart(m_audioFormat.SampleRate,
                                      m_audioFormat.Bitrate,
                                      m_audioFormat.Channels);
    }
    catch(GnError &e)
    {
        ALOG_ERR_CV("%s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
        internalError = true;
        goto GOTO_END;
    }

    sendSongRecognitionStatusEvent(GnServiceSongRecognitionStatus::STATUS_RUNNING);
    while ((isRunning()) && (emptyPipeReadCount < kMaxEmptyPipeReadRetries))
    {
        ssize_t bytesRead = read(m_audioFd, readBuffer, readSize);

        if (bytesRead > 0)
        {
            try
            {
                emptyPipeReadCount = 0;
                m_gnRadioId.AudioProcess(static_cast<const gnsdk_byte_t *>(readBuffer), bytesRead);
            }
            catch (GnError& e)
            {
                ALOG_ERR_CV("%s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription() );
                internalError = true;
                break;
            }
        }
        else
        {
            if (bytesRead < 0)
            {
                ALOG_ERR_CV("error reading audio data from file descriptor, %li", bytesRead);
                internalError = true;
                break;
            }
            else if (isRunning())
            {
                ALOG_DEBUG_CV("Job %lu empty pipe, try %i", getJobId(), ++emptyPipeReadCount);
                usleep(kSleepBetweenReadTries);
            }
        }
    }

GOTO_END:
    try
    {
        m_gnRadioId.AudioProcessStop();
    }
    catch (GnError& e) {
        ALOG_ERR_CV("%s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }

    if ( internalError )
    {
        sendSongRecognitionStatusEvent(GnServiceSongRecognitionStatus::STATUS_ERROR);
    }
    else
    {
        sendSongRecognitionStatusEvent(GnServiceSongRecognitionStatus::STATUS_STOPPED);
    }

    if ( close(m_audioFd) != 0 )
    {
        ALOG_WARN("audio file descriptor could not be closed");
    }

    if ( readBuffer != nullptr )
    {
        free(readBuffer);
    }

#ifdef GN_INTEGRATE_FRAMEWORK
        TBIRC::mmx::MetadataService::GnSongRecognitionStatus(TBIRC::mmx::MetadataService::EGnSongRecognition::GnSongRecognitionStopped);
#endif
    ALOG_DEBUG_CV("song recognition request task stopped%s", internalError ? " with error" : "");
    finished();
}

/* General use GnStatus callback (percent complete, network usage, etc.) See IGnStatusEvents for
 * more details */
void GnRequestSongRecognition::StatusEvent(GnStatus status,
                                           gnsdk_uint32_t percentComplete,
                                           gnsdk_size_t bytesTotalSent,
                                           gnsdk_size_t bytesTotalReceived,
                                           IGnCancellable& canceller)
{
    USE_CHANNEL();
    GNSDK_UNUSED(status);
    GNSDK_UNUSED(percentComplete);
    GNSDK_UNUSED(bytesTotalSent);
    GNSDK_UNUSED(bytesTotalReceived);
    GNSDK_UNUSED(canceller);
}

/* Callback delegate called when performing RadioId operations
 *
 * Everytime the SDK tries to identify the current song, the application will receive one of these
 * callbacks:
 * - musicIdStreamAlbumResult() with the matched album(s),
 * - musicIdStreamIdentifyingStatusEvent(kStatusIdentifyingNoNewResult) if the previously matched
 *   track is still playing.
 * - musicIdStreamIdentifyCompletedWithError() with the error information, in case something went
 *   wrong
 */
void GnRequestSongRecognition::RadioIdIdentifyingStatusEvent(GnRadioIdIdentifyingStatus status,
                                                             IGnCancellable& canceller)
{
    USE_CHANNEL();
    if ( m_cancelFlag )
    {
        //this request was already stopped by the client, no need to proceed any further
        return;
    }
    switch(status)
    {
        case gracenote::radioid::kRadioIdStatusIdentifyingInvalid: break;
        case gracenote::radioid::kRadioIdStatusIdentifyingStarted: ALOG_DEBUG("identifier started");
            sendIdentifierStatusEvent("identifier running");
            break;
        case gracenote::radioid::kRadioIdStatusIdentifyingFpGenerated: ALOG_DEBUG("fingerprint generated");
            sendIdentifierStatusEvent("fingerprint generated");
            break;
        case gracenote::radioid::kRadioIdStatusIdentifyingLocalQueryStarted:
        case gracenote::radioid::kRadioIdStatusIdentifyingLocalQueryEnded: break;
        case gracenote::radioid::kRadioIdStatusIdentifyingOnlineQueryStarted: ALOG_DEBUG("online query started");
            sendIdentifierStatusEvent("online query started");
            break;
        case gracenote::radioid::kRadioIdStatusIdentifyingOnlineQueryEnded: ALOG_DEBUG("online query ended");
            sendIdentifierStatusEvent("online query ended");
            break;
        case gracenote::radioid::kRadioIdStatusIdentifyingManagedQueryCreated: break;
        case gracenote::radioid::kRadioIdStatusIdentifyingEnded: ALOG_DEBUG("identifier finished");
            sendIdentifierStatusEvent("identifier finished");
            break;
    }

    GNSDK_UNUSED(canceller);
}

/* RadioId result available callback. Called when a new song is identified */
void GnRequestSongRecognition::RadioIdMatchUpdate(GnRadioIdResultStatus status,
                                                  GnResponseAlbums &albumResult)
{
    USE_CHANNEL();
    if ( m_cancelFlag )
    {
        //this request was already stopped by the client, no need to proceed any further
        return;
    }
    switch(status)
    {
        case gracenote::radioid::kStatusResultNewMatch:
            ALOG_INFO("Result Status: New Match");
            sendMatchStatusEvent("New Match");
            onEvent_NewMatch(albumResult);
            break;
        case gracenote::radioid::kStatusResultSameMatch:
            ALOG_INFO("Result Status: Same Match");
            sendMatchStatusEvent("Same Match");
            break;
        case gracenote::radioid::kStatusResultNoMatch:
            ALOG_INFO("Result Status: No Match");
            sendMatchStatusEvent("No Match");
            onEvent_ClearMatch();
            break;
        case gracenote::radioid::kStatusResultClearMatch:
            ALOG_INFO("Result Status: Clear Match");
            sendMatchStatusEvent("Clear Match");
            onEvent_ClearMatch();
            break;
        default:
            break;
    }
}

/* RadioId identification error callback. Called if the identification failed due to an error condition. */
void GnRequestSongRecognition::RadioIdError(GnError& e)
{
    USE_CHANNEL();
    ALOG_ERR_CV("Error: %s 0x%x, %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
}

void GnRequestSongRecognition::onEvent_NewMatch(const GnResponseAlbums &response)
{
    USE_CHANNEL();
    // clear all previously found data
    m_trackInfo.init();

    gracenote::metadata::album_iterator itAlbum = response.Albums().begin();

    // get the first match, it should be the best match
    if ( itAlbum != response.Albums().end() )
    {
        ALOG_INFO_CV("match found: title='%s', artist='%s', album='%s', track='%s', length=%i s, position = %i s",
                      itAlbum->TrackMatched().Title().Display(),
                      itAlbum->Artist().Name().Display(),
                      itAlbum->Title().Display(),
                      itAlbum->TrackMatched().TrackNumber(),
                      itAlbum->TrackMatched().Duration()/1000,
                      itAlbum->TrackMatched().CurrentPosition()/1000
                      );

        m_trackInfo.setAlbumName(string(itAlbum->Title().Display()));
        m_trackInfo.setArtistName(string(itAlbum->Artist().Name().Display()));
        m_trackInfo.setTrackName(string(itAlbum->TrackMatched().Title().Display()));
        m_trackInfo.setAlbumGenre(string(itAlbum->Genre(kDataLevel_2)));
        m_trackInfo.setAlbumYear(string(itAlbum->Year()));
        m_trackInfo.setTrackLength(itAlbum->TrackMatched().Duration() / 1000);
        m_trackInfo.setPlayPosition(itAlbum->TrackMatched().CurrentPosition() / 1000);

        getExternalIdsFromAlbum(*itAlbum, m_trackInfo);
        getExternalIdsFromTrack(itAlbum->TrackMatched(), m_trackInfo);
        getMoodFromTrack(itAlbum->TrackMatched(), m_trackInfo);
        getTempoFromTrack(itAlbum->TrackMatched(), m_trackInfo);
        getGnIdsFromTrack(itAlbum->TrackMatched(), m_trackInfo);

        getCoverFromAlbum(*itAlbum);

        m_recogListener->onRecognitionEvent_currentlyPlayingTrack(itAlbum->TrackMatched());
    }
    else
    {
        ALOG_WARN("no album found for response");
    }
}

void GnRequestSongRecognition::onEvent_ClearMatch()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    if ( m_recogListener == nullptr )
    {
        ALOG_WARN("no listener found, not sending event");
        return;
    }
    m_recogListener->onRecognitionEvent_clearResult(getJobId());

    ALOG_DEBUG("EXIT");
}

void GnRequestSongRecognition::sendRecognitionResultEvent(const unsigned char *data, const uint32_t length)
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    if ( m_recogListener == nullptr )
    {
        ALOG_WARN("no listener found, not sending event");
        return;
    }
    m_recogListener->onRecognitionEvent_dataReceived(getJobId(), m_trackInfo, data, length);
    ALOG_DEBUG("EXIT");
}

void GnRequestSongRecognition::sendIdentifierStatusEvent(const std::string &status)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("status='%s'", status.c_str());
    if ( m_recogListener == nullptr )
    {
        ALOG_WARN("no listener found, not sending event");
        return;
    }
    m_recogListener->onRecognitionEvent_identifierStatus(getJobId(), status);
}

void GnRequestSongRecognition::sendMatchStatusEvent(const std::string &status)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("status='%s'", status.c_str());
    if ( m_recogListener == nullptr )
    {
        ALOG_WARN("no listener found, not sending event");
        return;
    }
    m_recogListener->onRecognitionEvent_matchStatus(getJobId(), status);
}

void GnRequestSongRecognition::sendSongRecognitionStatusEvent(
        const GnServiceSongRecognitionStatus &status)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("status=%i", status);
    if ( m_recogListener == nullptr )
    {
        ALOG_WARN("no listener found, not sending event");
        return;
    }
    m_recogListener->onRecognitionEvent_generalStatus(getJobId(), status);
}

void GnRequestSongRecognition::getCoverFromAlbum(const GnAlbum &album)
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");

    GnImageSize requestedImageSize = kImageSizeMedium;
    GnAsset asset = album.Content(gracenote::kContentTypeImageCover).Asset(requestedImageSize);

    if ( asset.IsNull() )
    {
        ALOG_WARN("asset is null");
        sendRecognitionResultEvent(nullptr, 0);
        return;
    }

    gnsdk_cstr_t url = asset.UrlGnsdk();
    if ( (url != GNSDK_NULL) )
    {
        if ( strcmp("", url) != 0 )
        {
            auto pStatusEventHandler = new GnServiceStatusEventHandler(
                    const_cast<gnsdk_str_t>("Local Image Fetch"));
            GnAssetFetch assetFetch(*m_gnUser, url, kLookupModeLocal, pStatusEventHandler);
            if ((GNSDK_NULL != assetFetch.Data()) && (0 != assetFetch.Size()))
            {
                ALOG_INFO_CV("image found locally, type='%s'", assetFetch.Type());
                sendRecognitionResultEvent(const_cast<unsigned char *>(assetFetch.Data()),
                                           assetFetch.Size());
            }
            else
            {
                ALOG_INFO("no local image found, trying online");
                auto pStatusEventHandlerOnl = new GnServiceStatusEventHandler(
                        const_cast<gnsdk_str_t>("Online Image Fetch"));
                GnAssetFetch assetFetchOnline(*m_gnUser, url, kLookupModeOnlineNoCache,
                                              pStatusEventHandlerOnl);
                if ((GNSDK_NULL != assetFetchOnline.Data()) && (0 != assetFetchOnline.Size()))
                {
                    ALOG_INFO_CV("image found online, type='%s'", assetFetchOnline.Type());
                    sendRecognitionResultEvent(const_cast<unsigned char *>(assetFetchOnline.Data()),
                                               assetFetchOnline.Size());
                    if (m_resultDb != nullptr)
                    {
                        //add found album and image to the result db
                        m_resultDb->addAlbum(album);
                        ALOG_INFO("album written to result db");
                        m_resultDb->addImage(assetFetchOnline.Data(),
                                             assetFetchOnline.Size(),
                                             asset);
                        ALOG_INFO("image written to result db");
                    }
                }
                else
                {
                    sendRecognitionResultEvent(nullptr, 0);
                }
            }
        }
        else
        {
            ALOG_WARN("asset url is empty string");
            sendRecognitionResultEvent(nullptr, 0);
        }
    }
    else
    {
        ALOG_WARN("no asset url found");
        sendRecognitionResultEvent(nullptr, 0);
    }
    ALOG_DEBUG("EXIT");
}

void GnRequestSongRecognition::getExternalIdsFromTrack(const gracenote::metadata::GnTrack &track,
                                                       GnServiceTrackInfo &trackInfo)
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    for( const auto &it : track.ExternalIds() )
    {
        ALOG_DEBUG_CV("externalID: source=%s, value=%s", it.Source(), it.Value());
        GnServiceExternalId externalId(it.Source(), it.Value());
        trackInfo.addExternalId(externalId);
    }
    ALOG_DEBUG("EXIT");
}

void GnRequestSongRecognition::getExternalIdsFromAlbum(const gracenote::metadata::GnAlbum &album,
                                                       GnServiceTrackInfo &trackInfo)
{
    USE_CHANNEL();
    for( const auto &it : album.ExternalIds() )
    {
        ALOG_DEBUG_CV("album-externalID: type=%s, source=%s, value=%s",
                      it.Type(), it.Source(), it.Value() );
        GnServiceExternalId albumExternalId(it.Source(), it.Value());
        trackInfo.addExternalAlbumId(albumExternalId);
    }
}

void GnRequestSongRecognition::getGnIdsFromTrack(const gracenote::metadata::GnTrack &track,
                                                 GnServiceTrackInfo &trackInfo)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("GNID='%s', GNUID='%s'", track.GnId(), track.GnUId());
    trackInfo.setGnId(string(track.GnId()));
    trackInfo.setGnUId(string(track.GnUId()));
}

void GnRequestSongRecognition::getMoodFromTrack(const gracenote::metadata::GnTrack &track,
                                                GnServiceTrackInfo &trackInfo)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("Mood: level 1 = '%s', level 2 = '%s'",
                  track.Mood(kDataLevel_1), track.Mood(kDataLevel_2));
    trackInfo.setMood(string(track.Mood(kDataLevel_1)));
}

void GnRequestSongRecognition::getTempoFromTrack(const gracenote::metadata::GnTrack &track,
                                                 GnServiceTrackInfo &trackInfo)
{
    USE_CHANNEL();
    ALOG_INFO_CV("Tempo: level 1 = '%s', level 2 = '%s', level 3 = '%s'",
                  track.Tempo(kDataLevel_1), track.Tempo(kDataLevel_2), track.Tempo(kDataLevel_3));
    trackInfo.setTempo(string(track.Tempo(kDataLevel_3)));
}

void GnRequestSongRecognition::setCountryCode(const char *code)
{
    USE_CHANNEL();
    try
    {
        m_gnRadioId.Options().Custom("gnsdk_radioid_option_enable_lyrics_country_code_iso_alpha3",
                                     code);
        ALOG_INFO_CV("setting iso-alpha-3 country code to '%s'", code);
    }
    catch (GnError &e)
    {
        ALOG_WARN_CV("could not set country code: %s 0x%x : %s",
                    e.ErrorAPI(),
                    e.ErrorCode(),
                    e.ErrorDescription());
    }
}

