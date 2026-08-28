// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 03.12.19.
//

#ifndef GNSERVICE_RESULTDB_GNLOCALLOOKUPDB_HXX_
#define GNSERVICE_RESULTDB_GNLOCALLOOKUPDB_HXX_

#include <gnsdk.hpp>
#include "../GnServiceConstants.hxx"

namespace gnservice
{

class GnLocalLookupDB
{
public:

    struct gnServiceResultDbParam
    {
        std::string location;
        std::string id;
        gracenote::GnConfig config;
    };

    GnLocalLookupDB();

    uint32_t init(const std::string& location);
    void displayDbInfo() const;
    void addAlbum(const gracenote::metadata::GnAlbum& album);
    void addImage(const gnsdk_byte_t* imgData,
                  const gnsdk_size_t size,
                  const gracenote::metadata::GnAsset& asset);

private:
    uint32_t createFolders();
    uint32_t createDb();
    uint32_t retryCreateDb();
    uint32_t deleteAllDbFiles();
    void setFilePermissions(const std::string& path);

    void initialSizeCheck();
    void determineActiveDb();
    void checkSize();

    void reCreate(const uint32_t index);

    void displaySizeInfo() const;

    void setDbConfig(const uint32_t index);

    std::string m_location;
    gracenote::lookupdatabase::GnLookupDatabase* m_pGnLookupDB[kMaxNumberOfResultDb];
    gracenote::lookupdatabase::GnLookupDatabase* m_pActiveDb;
    uint32_t m_activeIndex;
    gnServiceResultDbParam m_DbConfig[kMaxNumberOfResultDb];
    bool m_initialized;
};
}//namespace gnservice

#endif //GNSERVICE_RESULTDB_GNLOCALLOOKUPDB_HXX_
