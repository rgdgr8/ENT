// Copyright 2025 <e.solutions GmbH>
// Copyright (c) 2026 Cariad SE
// All rights reserved.
//
// Originally developed by e.solutions GmbH (armo7765)
// Modified by: Christian Winter (T2-CH), Volkswagen Group / Cariad SE
//
// Modifications:
// - 2026-02-06: Removed eso.fw ipl::string dependency, using std::string instead

#ifndef METADATASERVICE_GNSERVICEIRCTRACING_HXX
#define METADATASERVICE_GNSERVICEIRCTRACING_HXX

#include <string>

namespace TBIRC {
namespace mmx {
namespace MetadataService {

    enum EGnOnlineUserStatus {
        GnOnlineUserStatusRegistered,
        GnOnlineUserStatusFailed
    };

    enum EGnSongRecognition {
        GnSongRecognitionStarted,
        GnSongRecognitionStopped
    };

    enum EPositioningData {
        PositioningDataInUse,
        PositioningDataNotInUse
    };

// Entry GnOnlineUserStatus
    void GnOnlineUserStatus (enum EGnOnlineUserStatus gnOnlineUserStatus);

// Entry GnSDKError
    static const size_t GnSDKError_error_max_strlen = 128 ;
    static const size_t GnSDKError_envData_max_strlen = 128 ;
    void GnSDKError (const std::string & error, const std::string & envData);

// Entry GnSongRecognitionStatus
    void GnSongRecognitionStatus (enum EGnSongRecognition gnSongRecognition);

// Entry PositioningData
    void PositioningData (enum EPositioningData positioningData);

} // end of namespace MetadataService
} // end of namespace mmx
} // end of namespace TBIRC

#endif //METADATASERVICE_GNSERVICEIRCTRACING_HXX
