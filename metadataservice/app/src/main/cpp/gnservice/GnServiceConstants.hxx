// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 15.05.2021.
//

#ifndef GNSERVICE_GNSERVICECONSTANTS_HXX_
#define GNSERVICE_GNSERVICECONSTANTS_HXX_

#include <gnsdk.hpp>

#if defined(VARIANT_OEM_AU)
#warning "using gracenote license for OEM AU"
    static const gnsdk_cstr_t gnLicenseString = "-- BEGIN LICENSE v1.0 B92A1759 --\n"
                                              "licensee: Gracenote, Inc.\n"
                                              "name: Cariad - OneInfotainment Standard EVO License File2\n"
                                              "\n"
                                              "client_id: 2011225880\n"
                                              "musicid_text: enabled\n"
                                              "musicid_cd: enabled\n"
                                              "local_images: enabled\n"
                                              "local_mood: enabled\n"
                                              "radioid: enabled\n"
                                              "radio_stations: enabled\n"
                                              "radio_no_location: enabled\n"
                                              "radio_nowplaying: enabled\n"
                                              "\n"
                                              "-- SIGNATURE B92A1759 --\n"
                                              "lAADAgAfARXsDqIuyznRKCCpfImw2zqltj86yIBabP3RX420NQAeARzyMCOYsdbPWJBYpVQBMbwfqvclUoK769ipSWeK\n"
                                              "-- END LICENSE B92A1759 --\n";
    static const gnsdk_cstr_t clientId    = "2011225880";
    static const gnsdk_cstr_t clientIdTag = "1DDC4CD68CAC20A2D9A6B665D3A93CCA";
    #define GN_LICENSE_VALID
/* #elif defined(VARIANT_OEM_PA)
    #warning "using gracenote license for OEM PA"
    static const gnsdk_cstr_t gnLicenseString = "-- BEGIN LICENSE v1.0 B92A1759 --\n"
                                                "licensee: Gracenote, Inc.\n"
                                                "name: Cariad - OneInfotainment Standard EVO License File2\n"
                                                "\n"
                                                "client_id: 2011225880\n"
                                                "musicid_text: enabled\n"
                                                "musicid_cd: enabled\n"
                                                "local_images: enabled\n"
                                                "local_mood: enabled\n"
                                                "radioid: enabled\n"
                                                "radio_stations: enabled\n"
                                                "radio_no_location: enabled\n"
                                                "radio_nowplaying: enabled\n"
                                                "\n"
                                                "-- SIGNATURE B92A1759 --\n"
                                                "lAADAgAfARXsDqIuyznRKCCpfImw2zqltj86yIBabP3RX420NQAeARzyMCOYsdbPWJBYpVQBMbwfqvclUoK769ipSWeK\n"
                                                "-- END LICENSE B92A1759 --\n";
    static const gnsdk_cstr_t clientId    = "2011225880";
    static const gnsdk_cstr_t clientIdTag = "1DDC4CD68CAC20A2D9A6B665D3A93CCA";
    #define GN_LICENSE_VALID */
#else
    #warning "using EMPTY gracenote license"
    static const gnsdk_cstr_t gnLicenseString = "";
    static const gnsdk_cstr_t clientId    = "";
    static const gnsdk_cstr_t clientIdTag = "";
#endif

static const char kChinaProxyUrl[] = "https://gnproxy.services.gnmedia.cn:443";
static const char kCountryToUseChinaProxy[] = "CN";

/* path for gracenote backend root certificate file */
static const char kCertificateFilePath[] = "/truststore/truststore_gracenote.pem";
static const char kCertificateOtaFilePath[] = "/truststore_ota/truststore_gracenote.pem";

/* constant values for result db */
static const char kResultDbMainLocation[] = "resultDb";
static const char kRadioResultDbMainLocation[] = "radioResultDb";
static const uint32_t kMaxNumberOfResultDb = 2;
static const uint32_t kDbFolderAccessMode = 0770;
static const uint32_t kResultDbMaxSize = 1024 * 1024 * 100;     // 100 MB for media result db (coverart etc.)
static const uint32_t kRadioDbMaxSize = 1024 * 1024 * 60;       // 60 MB for metadata of radio stations
static const uint32_t kRadioImgDbMaxSize = 1024 * 1024 * 60;    // 60 MB for images of radio stations, makes 120 MB alltogether
static const gnsdk_cstr_t kRadioDbExpirationTime = "720";       // expiration time in hours, 720 hours are 30 days
static const char* kResultDbFiles[] = { "gn_cds.gdb", "gn_itxt.gdb", "gn_mdata.gdb"};
static const char* kRadioResultDbMetaFile = "gn_rsimd.gdb";
static const char* kRadioResultDbImgFile = "gn_rscds.gdb";

static const uint8_t kMaxEmptyPipeReadRetries = 25;     // maximum allowed consecutive reads on pipe with 0 return
static const useconds_t kSleepBetweenReadTries = 10;      // 10 microseconds

struct gnServiceResultDbConfig {
    std::string identifier;
    std::string subPath;
};

static const gnServiceResultDbConfig resultDbConfig[kMaxNumberOfResultDb] = {
        { "resultDb_a", "a" },
        {"resultDb_b", "b"}
};

struct gnServiceRadioResultDbConfig {
    std::string subPath;
    std::string subPathMeta;
    std::string subPathImg;
    std::string identifierMeta;
    std::string identifierImg;
};

static const gnServiceRadioResultDbConfig radioResultDbConfig[] = {
        {"a", "data", "img", "resultDbMeta_a", "resultDbImg_a"},
        { "b", "data", "img","resultDbMeta_b", "resultDbImg_b"}
};

#endif //GNSERVICE_GNSERVICECONSTANTS_HXX_
