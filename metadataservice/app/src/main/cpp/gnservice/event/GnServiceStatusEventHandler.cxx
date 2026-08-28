// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 03.12.19.
//

#include <gnsdk.hpp>
#include "../helper/GnServiceLogging.hxx"
#include "GnServiceStatusEventHandler.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Event
DECLARE_CHANNEL();
#define LOG_TAG "GnStatusEventHandler"

using gracenote::GnStatus;
using gracenote::IGnCancellable;

/*-----------------------------------------------------------------------------
 *	class StatusEvent Handler
 *
 *	This callback will be triggered by GNSDK when it is completing
 *	long running activity. For example, queries (local and online),
 *	image fetching, list downloading, etc.
 *
 *	Only kStatusBegin and kStatusComplete are implemented in this example.
 */
GnServiceStatusEventHandler::GnServiceStatusEventHandler(gnsdk_str_t callbackData)
{
    m_callbackData = callbackData;
}

void GnServiceStatusEventHandler::StatusEvent(GnStatus status,
                                              gnsdk_uint32_t percentComplete,
                                              gnsdk_size_t bytesTotalSent,
                                              gnsdk_size_t bytesTotalReceived,
                                              IGnCancellable& canceller)
{
    USE_CHANNEL();
    GNSDK_UNUSED(percentComplete);
    GNSDK_UNUSED(bytesTotalSent);
    GNSDK_UNUSED(bytesTotalReceived);

    if (status == gracenote::kStatusBegin)
    {
        ALOG_DEBUG_CV("Status: %s begin\n", m_callbackData);
    }
    else if (status == gracenote::kStatusComplete)
    {
        ALOG_DEBUG_CV("Status: %s completed\n", m_callbackData);
    }
    else
    {
        // See GnStatus for the full list of status enums
    }
    // To cancel any long running GNSDK activity, set this to GNSDK_TRUE before returning.
    // Setting abort to true can happen on any status enum.
    canceller.SetCancel(false);
}