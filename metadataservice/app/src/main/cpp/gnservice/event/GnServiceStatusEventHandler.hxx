// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 03.12.19.
//

#ifndef GNSERVICE_EVENT_GNSERVICESTATUSEVENTHANDLER_HXX_
#define GNSERVICE_EVENT_GNSERVICESTATUSEVENTHANDLER_HXX_

#include <gnsdk.hpp>


/*-----------------------------------------------------------------------------
 *	class StatusEvent Handler
 *
 *	This callback will be triggered by GNSDK when it is completing
 *	long running activity. For example, queries (local and online),
 *	image fetching, list downloading, etc.
 *
 *	Only kStatusBegin and kStatusComplete are implemented in this example.
 */
class GnServiceStatusEventHandler : public gracenote::IGnStatusEvents
{
public:
    explicit GnServiceStatusEventHandler(gnsdk_str_t callbackData);

    void StatusEvent(gracenote::GnStatus status,
                     gnsdk_uint32_t percentComplete,
                     gnsdk_size_t bytesTotalSent,
                     gnsdk_size_t bytesTotalReceived,
                     gracenote::IGnCancellable& canceller);

private:
    gnsdk_str_t m_callbackData;

};


#endif //GNSERVICE_EVENT_GNSERVICESTATUSEVENTHANDLER_HXX_
