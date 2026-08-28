// Copyright 2023 <e.solutions GmbH>
// Created by miir1367 on 23.05.2023.
//

#ifndef GNSERVICE_REQUEST_GNREQUESTLYRICS_HXX_
#define GNSERVICE_REQUEST_GNREQUESTLYRICS_HXX_

#include "GnRequest.hxx"
#include "../event/IGnLyricsEventListener.hxx"
#include "../helper/GnServiceDataTypes.hxx"
#include "../resultdb/GnLocalLookupDb.hxx"

class GnRequestLyrics : public GnRequest
{
public:
    GnRequestLyrics(IGnLyricsEventListener* listener,
                    gracenote::GnUser* user,
                    gnservice::GnLocalLookupDB* resultDb,
                    gracenote::metadata::GnTrack& track,
                    GnServiceLyricsType type,
                    bool familyFriendly);

    void setCurrentTrack(const gracenote::metadata::GnTrack& track);

private:
    void executeTask() override;

    void getLyrics();

    gracenote::metadata::GnAsset getAssetForLyrics(const gracenote::metadata::GnContent& content);

    void sendResultEvent(const std::vector<unsigned char>& lyricsData);

private:
    IGnLyricsEventListener* m_listener;
    gracenote::metadata::GnTrack m_curTrack;
    GnServiceLyricsType m_type;
    bool m_familyFriendly;
    bool m_trackUpdated;
};


#endif //GNSERVICE_REQUEST_GNREQUESTLYRICS_HXX_
