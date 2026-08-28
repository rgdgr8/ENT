// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 03.06.2020.
//

#ifndef GNSERVICE_REQUEST_GNREQUESTSONGRECOGNITION_HXX_
#define GNSERVICE_REQUEST_GNREQUESTSONGRECOGNITION_HXX_

#include <string>

#include "GnRequest.hxx"
#include "../helper/GnServiceTrackInfo.hxx"
#include "../event/IGnSongRecognitionEventListener.hxx"

struct GnServiceAudioFormat
{
    uint32_t SampleRate;
    uint32_t Bitrate;
    uint32_t Channels;
};

class GnRequestSongRecognition : public GnRequest,
                                 public gracenote::radioid::IGnRadioIdEvents
{
public:
    GnRequestSongRecognition(IGnSongRecognitionEventListener* recogListener,
                             gracenote::GnUser* user,
                             gnservice::GnLocalLookupDB* resultDb,
                             std::string path);

    void setAudioFormat(GnServiceAudioFormat audioFormat);

    void setAudioFileDescriptor(int32_t audioFileDesc);

    // inherited from base class GnRequest
    virtual void executeTask();

    // inherited from IGnRadioIdEvents
    /* General use GnStatus callback (percent complete, network usage, etc).
     * See IGnStatusEvents for more details */
    virtual void StatusEvent(gracenote::GnStatus status,
                             gnsdk_uint32_t percentComplete,
                             gnsdk_size_t bytesTotalSent,
                             gnsdk_size_t bytesTotalReceived,
                             gracenote::IGnCancellable& canceller);

    /* RadioId identification status callback (identification start, end, etc.).
     * See GnRadioIdIdentifyingStatus for all available status messages. */
    void RadioIdIdentifyingStatusEvent(gracenote::radioid::GnRadioIdIdentifyingStatus status,
                                       gracenote::IGnCancellable& canceller);

    /* RadioId result available callback. Called when a new song is identified */
    void RadioIdMatchUpdate(gracenote::radioid::GnRadioIdResultStatus status,
                            gracenote::metadata::GnResponseAlbums& albumResult);

    /* RadioId identification error callback. Called if the identification failed due to an error
     * condition */
    void RadioIdError(gracenote::GnError& e);

    /* country code is necessary in order to retrieve lyrics for found track */
    void setCountryCode(const char* code);

private:
    // sending events to listener
    void sendSongRecognitionStatusEvent(const GnServiceSongRecognitionStatus& status);
    void sendRecognitionResultEvent(const unsigned char* data, const uint32_t length);
    void sendIdentifierStatusEvent(const std::string& status);
    void sendMatchStatusEvent(const std::string& status);

    void getCoverFromAlbum(const gracenote::metadata::GnAlbum& album);
    static void getExternalIdsFromTrack(const gracenote::metadata::GnTrack& track,
                                 GnServiceTrackInfo& trackInfo);
    static void getExternalIdsFromAlbum(const gracenote::metadata::GnAlbum& album,
                                        GnServiceTrackInfo& trackInfo);
    static void getGnIdsFromTrack(const gracenote::metadata::GnTrack& track,
                                  GnServiceTrackInfo& trackInfo);
    static void getMoodFromTrack(const gracenote::metadata::GnTrack& track,
                          GnServiceTrackInfo& trackInfo);
    static void getTempoFromTrack(const gracenote::metadata::GnTrack& track,
                           GnServiceTrackInfo& trackInfo);

    void onEvent_NewMatch(const gracenote::metadata::GnResponseAlbums& response);
    void onEvent_ClearMatch();

private:
    IGnSongRecognitionEventListener* m_recogListener;
    gracenote::radioid::GnRadioId m_gnRadioId;
    std::string m_path;
    int32_t m_audioFd;
    GnServiceAudioFormat m_audioFormat;
    GnServiceTrackInfo m_trackInfo;
};


#endif //GNSERVICE_REQUEST_GNREQUESTSONGRECOGNITION_HXX_
