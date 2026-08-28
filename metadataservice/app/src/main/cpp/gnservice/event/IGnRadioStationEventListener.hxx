// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 21.06.2021.
//

#ifndef GNSERVICE_EVENT_IGNRADIOSTATIONEVENTLISTENER_HXX_
#define GNSERVICE_EVENT_IGNRADIOSTATIONEVENTLISTENER_HXX_

#include <vector>

class IGnRadioStationEventListener
{
public:
    virtual ~IGnRadioStationEventListener(){}
    virtual void onRadioStationEvent_status(const GnServiceRadioStationStatus status) const = 0;
    virtual void onRadioStationEvent_dataReceived(const GnServiceRadioStation& radioStation,
                                                  const std::vector<unsigned char>& imageData) const = 0;
};

#endif //GNSERVICE_EVENT_IGNRADIOSTATIONEVENTLISTENER_HXX_
