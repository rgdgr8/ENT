//
// Created by mike on 21.10.21.
// Copyright (2021) <michael@irsiegler.de>
//

#ifndef GNSERVICE_RADIORESULTDB_GNRADIORESULTDB_HXX_
#define GNSERVICE_RADIORESULTDB_GNRADIORESULTDB_HXX_

#include "../GnServiceConstants.hxx"

class GnRadioResultDb
{
public:

    struct gnRadioResultDbParam {
        std::string location;
        std::string pathMeta;
        std::string pathImg;
        std::string idMeta;
        std::string idImg;
        gracenote::GnConfig configMeta;
        gracenote::GnConfig configImg;
    };

    GnRadioResultDb();

    uint32_t init(const std::string& location);
    void checkAndUpdateStations(gracenote::radioid::GnRadioId& radioId);

    void addImage(const gnsdk_byte_t* imgData, const gnsdk_size_t size, const gracenote::metadata::GnAsset& asset);

private:
    uint32_t createFolders();
    uint32_t createDb();
    uint32_t retryCreateDb();
    uint32_t deleteAllDbFiles();
    void setFilePermissions(const std::string& path);
    void displayMetaDbInfo();
    void displayImgDbInfo();
    void checkMetaDbSize();
    void checkImgDbSize();
    void displayMetaDbSize();
    void displayImgDbSize();
    void reCreateImgDb(const uint32_t index);
    void reCreateMetaDb(const uint32_t index);
    void setDbConfigMeta(const uint32_t index);
    void setDbConfigImg(const uint32_t index);

private:
    std::string m_location;
    gracenote::lookupdatabase::GnLookupDatabase* m_pDbMeta[kMaxNumberOfResultDb];
    gracenote::lookupdatabase::GnLookupDatabase* m_pDbImg[kMaxNumberOfResultDb];
    gracenote::lookupdatabase::GnLookupDatabase* m_pActiveDbMeta;
    gracenote::lookupdatabase::GnLookupDatabase* m_pActiveDbImg;
    uint32_t m_activeIndexMeta;
    uint32_t m_activeIndexImg;
    gnRadioResultDbParam m_DbConfig[kMaxNumberOfResultDb];
    bool m_initialized;
};


#endif //GNSERVICE_RADIORESULTDB_GNRADIORESULTDB_HXX_
