// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 01.12.19.
//

#ifndef GNSERVICE_HELPER_GNSDKLOGEVENTS_HXX_
#define GNSERVICE_HELPER_GNSDKLOGEVENTS_HXX_

#include <gnsdk.hpp>

namespace gnservice
{
class GnsdkLogEvents : public gracenote::IGnLogEvents
    {
    public:
        GnsdkLogEvents();

        bool LogMessage(gnsdk_uint16_t packageId, gracenote::GnLogMessageType messageType,
                        gnsdk_uint32_t errorCode, gnsdk_cstr_t message);

    };
}//namespace gnservice

#endif //GNSERVICE_HELPER_GNSDKLOGEVENTS_HXX_
