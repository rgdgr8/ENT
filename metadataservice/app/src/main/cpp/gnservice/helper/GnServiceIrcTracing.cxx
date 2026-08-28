// Copyright 2025 <e.solutions GmbH>
// Copyright (c) 2026 Cariad SE
// All rights reserved.
//
// Originally developed by e.solutions GmbH (armo7765)
// Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
//
// Modifications:
// - 2026-02-06: Replaced IRC tracing with Android Log for standalone build

#include <android/log.h>
#include "GnServiceIrcTracing.hxx"

#define LOG_TAG "GnServiceIRC"
#define IRC_LOG(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

namespace TBIRC {
namespace mmx {
namespace MetadataService {

void GnOnlineUserStatus(enum EGnOnlineUserStatus gnOnlineUserStatus) {
    const char *_gnOnlineUserStatus;
    switch(gnOnlineUserStatus) {
        case GnOnlineUserStatusRegistered:
            _gnOnlineUserStatus="GnOnlineUserStatusRegistered";
            break;
        case GnOnlineUserStatusFailed:
            _gnOnlineUserStatus="GnOnlineUserStatusFailed";
            break;
        default:
            _gnOnlineUserStatus="?";
            break;
    }

    IRC_LOG("GnOnlineUserStatus: %s", _gnOnlineUserStatus);
}

void GnSDKError(const std::string & error, const std::string & envData) {
    IRC_LOG("GnSDKError: \"%s\" \"%s\"", error.c_str(), envData.c_str());
}

void GnSongRecognitionStatus(enum EGnSongRecognition gnSongRecognition) {
    const char *_gnSongRecognition;
    switch(gnSongRecognition) {
        case GnSongRecognitionStarted:
            _gnSongRecognition="GnSongRecognitionStarted";
            break;
        case GnSongRecognitionStopped:
            _gnSongRecognition="GnSongRecognitionStopped";
            break;
        default:
            _gnSongRecognition="?";
            break;
    }

    IRC_LOG("GnSongRecognitionStatus: %s", _gnSongRecognition);
}

void PositioningData(enum EPositioningData positioningData) {
    const char *_positioningData;
    switch(positioningData) {
        case PositioningDataInUse:
            _positioningData="PositioningDataInUse";
            break;
        case PositioningDataNotInUse:
            _positioningData="PositioningDataNotInUse";
            break;
        default:
            _positioningData="?";
            break;
    }

    IRC_LOG("PositioningData: %s", _positioningData);
}

} // end of namespace MetadataService
} // end of namespace mmx
} // end of namespace TBIRC