// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 15.06.2020.
//
#include <string>

#ifndef GNSERVICE_EVENT_IGNSONGRECOGNITIONEVENTLISTENER_HXX_
#define GNSERVICE_EVENT_IGNSONGRECOGNITIONEVENTLISTENER_HXX_

class IGnSongRecognitionEventListener
{
public:
    virtual ~IGnSongRecognitionEventListener(){}
    virtual void onRecognitionEvent_generalStatus(const uint64_t jobId,
                                                  const GnServiceSongRecognitionStatus& status) = 0;
    virtual void onRecognitionEvent_identifierStatus(const uint64_t jobId,
                                                     const std::string& status) const = 0;
    virtual void onRecognitionEvent_matchStatus(const uint64_t jobId,
                                                const std::string& status) const = 0;
    virtual void onRecognitionEvent_clearResult(const uint64_t jobId) = 0;
    virtual void onRecognitionEvent_dataReceived(const uint64_t jobId,
                                                 const GnServiceTrackInfo& trackInfo,
                                                 const unsigned char* imageData,
                                                 const uint32_t length) const = 0;
    virtual void onRecognitionEvent_currentlyPlayingTrack(const gracenote::metadata::GnTrack& track) = 0;
};

#endif //GNSERVICE_EVENT_IGNSONGRECOGNITIONEVENTLISTENER_HXX_
