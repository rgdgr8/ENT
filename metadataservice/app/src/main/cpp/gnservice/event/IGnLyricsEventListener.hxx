// Copyright 2023 <e.solutions GmbH>
// Created by miir1367 on 23.05.2023.
//

#ifndef GNSERVICE_EVENT_IGNLYRICSEVENTLISTENER_HXX
#define GNSERVICE_EVENT_IGNLYRICSEVENTLISTENER_HXX

class IGnLyricsEventListener
{
public:
    virtual ~IGnLyricsEventListener(){}

    virtual void onLyricsEvent_dataReceived(const std::vector<unsigned char>& lyricsData,
                                            const uint64_t jobId) = 0;
    virtual void onLyricsEvent_finished(const uint64_t jonId) = 0;
};

#endif //GNSERVICE_EVENT_IGNLYRICSEVENTLISTENER_HXX
