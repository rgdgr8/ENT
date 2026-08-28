// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 01.12.19.
//

#include "gnsdk.hpp"
#include "GnServiceLogging.hxx"

#include "GnsdkLogEvents.hxx"

#define LOG_TAG "GnsdkLogEvents"

using gracenote::GnLogMessageType;
using gnservice;

GnsdkLogEvents::GnsdkLogEvents()
{

}

bool GnsdkLogEvents::LogMessage(gnsdk_uint16_t packageId, GnLogMessageType messageType,
                                gnsdk_uint32_t errorCode, gnsdk_cstr_t message)
{
    switch(messageType) {
        case kLoggingMessageTypeError:
            ALOG_ERR_CV("packageid: 0x%x, error: 0x%x - %s", packageId, errorCode, message);
            break;
        case kLoggingMessageTypeWarning:
            ALOG_WARN_CV("packageid: 0x%x, warning: 0x%x - %s", packageId, errorCode, message);
            break;
        case kLoggingMessageTypeInfo:
            ALOG_INFO_CV("packageid: 0x%x, info: 0x%x - %s", packageId, errorCode, message);
            break;
        case kLoggingMessageTypeDebug:
            ALOG_DEBUG_CV("packageid: 0x%x, debug: 0x%x - %s", packageId, errorCode, message);
            break;
        default:
            break;
    }

    return true;

}