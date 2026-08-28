// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 31.05.20.
//

#ifndef GNSERVICE_EVENT_IGNCOVERARTEVENTLISTENER_HXX_
#define GNSERVICE_EVENT_IGNCOVERARTEVENTLISTENER_HXX_

#include <vector>

class IGnCoverArtEventListener
{
public:
    virtual ~IGnCoverArtEventListener(){}

    virtual void onCoverArtEvent_dataReceived(const GnServiceAlbumInfo& albumInfo,
                                              const std::vector<unsigned char>& imageData,
                                              const uint64_t jobId) = 0;
};

#endif //GNSERVICE_EVENT_IGNCOVERARTEVENTLISTENER_HXX_
