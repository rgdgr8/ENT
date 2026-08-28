// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 03.12.19.
//
#include <memory>
#include <string>
#include <sys/stat.h>

#ifdef GN_INTEGRATE_FRAMEWORK
#include <osal/FileSys.hxx>
#endif

#include "GnLocalLookupDb.hxx"

#include "../helper/GnServiceLogging.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ ResultDb_Local
DECLARE_CHANNEL();
#define LOG_TAG "GnLocalLookupDb"

using std::string;
using std::recursive_mutex;
using std::make_unique;
using std::lock_guard;
using gnservice::GnLocalLookupDB;
using gracenote::lookupdatabase::GnLookupDatabase;
using gracenote::metadata::GnDataObject;
using gracenote::GnString;
using gracenote::GnError;
using gracenote::metadata::GnRenderOptions;

static recursive_mutex resultDbMutex;

GnLocalLookupDB::GnLocalLookupDB() :
    m_location(),
    m_pGnLookupDB(),
    m_pActiveDb(),
    m_activeIndex(0),
    m_DbConfig(),
    m_initialized(false)
{
}

uint32_t GnLocalLookupDB::init(const string& location)
{
    USE_CHANNEL();
    lock_guard<recursive_mutex> lock(resultDbMutex);
    uint32_t res;

    if ( m_initialized )
    {
        ALOG_INFO("already initialized nothing to do");
        return 0;
    }

    m_location = location;

    res = createFolders();
    if ( res != 0 )
    {
        return res;
    }

    res = createDb();
    if ( res != 0 )
    {
        return res;
    }

    initialSizeCheck();

    determineActiveDb();

    m_initialized = true;

    ALOG_DEBUG("EXIT");
    return res;
}

uint32_t GnLocalLookupDB::createFolders()
{
    USE_CHANNEL();
    string resultDbBasePath = m_location + "/" + kResultDbMainLocation;

    ALOG_INFO_CV("trying to set up path '%s' for result db", resultDbBasePath.c_str());
    if ( mkdir(resultDbBasePath.c_str(), kDbFolderAccessMode) != 0 )
    {
        if(errno != EEXIST)
        {
            ALOG_ERR_CV("could not create directory for result db, errno=%i", errno);
            return 1;
        }
    }

    for ( uint32_t index = 0; index < kMaxNumberOfResultDb; index++ )
    {
        string resultDbPath = resultDbBasePath + "/" +  resultDbConfig[index].subPath;

        ALOG_DEBUG_CV("trying to set up path '%s' for result db", resultDbPath.c_str());
        if ( mkdir(resultDbPath.c_str(), kDbFolderAccessMode) )
        {
            if (errno != EEXIST) {
                ALOG_ERR_CV("could not create directory '%s' for result db, errno=%i",
                            resultDbPath.c_str(), errno);
                return 1;
            }

            ALOG_INFO_CV("created path for result db %s: '%s'",
                         resultDbConfig[index].identifier.c_str(),
                         resultDbPath.c_str());
        }

        m_DbConfig[index].location = resultDbPath;
        m_DbConfig[index].id = resultDbConfig[index].identifier;
    }

    return 0;
}

uint32_t GnLocalLookupDB::createDb()
{
    USE_CHANNEL();

    try
    {
        for (uint32_t i = 0; i < kMaxNumberOfResultDb; i++) {
            ALOG_INFO_CV("trying to create result db %s", m_DbConfig[i].id.c_str());
            setDbConfig(i);
            m_pGnLookupDB[i] = new GnLookupDatabase(m_DbConfig[i].id.c_str(),
                                                    m_DbConfig[i].config);

            // setting permissions for all files found in the db directory
            setFilePermissions(m_DbConfig[i].location);
        }
    }
    catch ( GnError& e)
    {
        ALOG_ERR_CV("could not create result db: error: %s, 0x%x %s",
                    e.ErrorAPI(),
                    e.ErrorCode(),
                    e.ErrorDescription());
        if ( retryCreateDb() )
        {
            return 1;
        }
    }
    return 0;
}

void GnLocalLookupDB::determineActiveDb()
{
    USE_CHANNEL();
    m_activeIndex = (m_pGnLookupDB[0]->Size() < kResultDbMaxSize/2) ? 0 : 1;
    m_pActiveDb = m_pGnLookupDB[m_activeIndex];

    ALOG_INFO_CV("using result db %s", m_activeIndex == 0 ? "a" : "b");
}

void GnLocalLookupDB::displayDbInfo() const
{
    USE_CHANNEL();
    if ( !m_initialized )
    {
        ALOG_DEBUG("result db not initialized");
        return;
    }

    GnDataObject info = m_pActiveDb->Info();
    GnRenderOptions render;
    GnString infoStr = info.Render(render.Xml());

    ALOG_INFO_CV("%s", infoStr.c_str());
    ALOG_INFO_CV("Size: %llu (%llu kB), max size %u (%u kB)",
                 m_pActiveDb->Size(),
                 m_pActiveDb->Size()/1024,
                 kResultDbMaxSize/2,
                 (kResultDbMaxSize/2)/1024);
}

void GnLocalLookupDB::addAlbum(const gracenote::metadata::GnAlbum& album)
{
    USE_CHANNEL();
    lock_guard<recursive_mutex> lock(resultDbMutex);

    if ( !m_initialized )
    {
        ALOG_WARN("result db not initialized");
        return;
    }

    ALOG_INFO_CV("writing album to result db %s", m_activeIndex == 0 ? "a" : "b");

    try
    {
        m_pActiveDb->AddRecord(album);
        checkSize();
    }
    catch ( GnError& e)
    {
        ALOG_ERR_CV("could not store album in result db: %s, error: 0x%x %s",
                      e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }
}

void GnLocalLookupDB::addImage(const gnsdk_byte_t* imgData,
                               const gnsdk_size_t size,
                               const gracenote::metadata::GnAsset& asset)
{
    USE_CHANNEL();
    lock_guard<recursive_mutex> lock(resultDbMutex);

    if (!imgData)
    {
        ALOG_WARN("Image data pointer is invalid.");
        return;
    }

    if ( !m_initialized )
    {
        ALOG_WARN("result db not initialized");
        return;
    }

    ALOG_INFO_CV("writing image to result db %s", m_activeIndex == 0 ? "a" : "b");

    try
    {
        m_pActiveDb->AddImage(imgData, size, asset);
        checkSize();
    }
    catch ( GnError& e)
    {
        ALOG_ERR_CV("could not store image in resultDb: %s, error: 0x%x %s",
                    e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }
}

void GnLocalLookupDB::displaySizeInfo() const
{
    USE_CHANNEL();
    ALOG_INFO_CV("%s size: %llu (%llu kB), max. size %u (%u kB)",
                 m_DbConfig[m_activeIndex].id.c_str(),
                 m_pActiveDb->Size(),
                 m_pActiveDb->Size()/1024,
                 kResultDbMaxSize/2,
                 (kResultDbMaxSize/2)/1024);
}

void GnLocalLookupDB::checkSize()
{
    displaySizeInfo();

    lock_guard<recursive_mutex> lock(resultDbMutex);
    if ( m_pActiveDb->Size() >= kResultDbMaxSize/2 )
    {
        USE_CHANNEL();
        uint32_t oldIndex = m_activeIndex;
        uint32_t newIndex = m_activeIndex == 0 ? 1 : 0;

        ALOG_INFO_CV("%s has reached maximum size, switching to next result db %s (new index = %u)",
                     m_DbConfig[oldIndex].id.c_str(),
                     m_DbConfig[newIndex].id.c_str(),
                     newIndex);
        reCreate(newIndex);
        displayDbInfo();
    }
}

void GnLocalLookupDB::initialSizeCheck()
{
    USE_CHANNEL();
    uint64_t totalSize = 0;
    for ( auto & i : m_pGnLookupDB )
    {
        totalSize += i->Size();
    }

    if ( totalSize > kResultDbMaxSize )
    {
        ALOG_WARN_CV("result db exceeds maximum allowed size, %lu kB (max. %d kB), deleting it",
                     totalSize/1024,
                     kResultDbMaxSize/1024);
        for ( uint32_t i=0; i < kMaxNumberOfResultDb; i++ )
        {
            reCreate(i);
        }
    }
}

void GnLocalLookupDB::reCreate(const uint32_t index)
{
    try
    {
        m_pGnLookupDB[index]->Close();
        gracenote::lookupdatabase::GnLookupDatabase::Delete(m_DbConfig[index].config);
        m_pGnLookupDB[index] = new GnLookupDatabase(m_DbConfig[index].id.c_str(),
                                                    m_DbConfig[index].config);
        m_pActiveDb = m_pGnLookupDB[index];
        m_activeIndex = index;
    }
    catch (GnError& e)
    {
        USE_CHANNEL();
        ALOG_ERR_CV("Error recreating result db: %s, error: 0x%x %s",
                    e.ErrorAPI(),
                    e.ErrorCode(),
                    e.ErrorDescription());
    }
}

void GnLocalLookupDB::setFilePermissions(const std::string &path)
{
    USE_CHANNEL();

#ifdef GN_INTEGRATE_FRAMEWORK
    for ( const char* fileName : kResultDbFiles )
    {
        string filePath = path + "/" + fileName;
        if (osal::FileSys::changeAccess(filePath,
                                        osal::FileAccess::owner_read |
                                        osal::FileAccess::owner_write |
                                        osal::FileAccess::group_read |
                                        osal::FileAccess::group_write))
        {
            ALOG_ERR_CV("could not set permissions for %s/%s", path.c_str(),
                        fileName);
        }
    }
#else
    const string chmodCommand("chmod 0660 " + path + "/*");
    ALOG_INFO_CV("executing chmod command '%s'", chmodCommand.c_str());
    std::system(chmodCommand.c_str());
#endif
}

uint32_t GnLocalLookupDB::retryCreateDb()
{
    USE_CHANNEL();
    if ( deleteAllDbFiles() )
    {
        return 1;
    }

    for ( uint32_t i = 0; i < kMaxNumberOfResultDb; i++)
    {
        try
        {
            ALOG_DEBUG_CV("trying to create result db %s", m_DbConfig[i].id.c_str());
            setDbConfig(i);
            m_pGnLookupDB[i] = new GnLookupDatabase(m_DbConfig[i].id.c_str(), m_DbConfig[i].config);
           
        }
        catch (GnError& e)
        {
            ALOG_ERR_CV("could not create result db: error: %s, 0x%x %s",
                        e.ErrorAPI(),
                        e.ErrorCode(),
                        e.ErrorDescription());
            return 1;
        }

      // setting permissions for all files found in the db directory
        setFilePermissions(m_DbConfig[i].location);
    }
    return 0;
}

uint32_t GnLocalLookupDB::deleteAllDbFiles()
{
    USE_CHANNEL();
    uint32_t res = 0;
    ALOG_WARN("deleting all result database files");

    for ( uint32_t index = 0; index < kMaxNumberOfResultDb; index++ )
    {
        // in case some of the db files have been opened already, close them
        try
        {
            if (m_pGnLookupDB[index] != nullptr) {
                m_pGnLookupDB[index]->Close();
                m_pGnLookupDB[index] = nullptr;
            }
        }
        catch (GnError& e)
        {
            ALOG_ERR_CV(" could not close result db, error: %s, 0x%x %s",
                        e.ErrorAPI(),
                        e.ErrorCode(),
                        e.ErrorDescription());
        }
        // delete all corresponding files
        for (const char* fileName : kResultDbFiles )
        {
            string filePath = m_DbConfig[index].location + "/" + fileName;
            if ( std::remove(filePath.c_str()) )
            {
                res = 1;
            }
        }
    }

    return res;
}

void GnLocalLookupDB::setDbConfig(const uint32_t i)
{
    m_DbConfig[i].config.LookupDatabase().StorageLocation(
            gracenote::kConfigLookupDatabaseLocationAll,
            m_DbConfig[i].location.c_str());
    m_DbConfig[i].config.LookupDatabase().EnableFeature(gracenote::kConfigLookupDatabaseFeatureEnableMusicIDText,
                                                        true);
    m_DbConfig[i].config.LookupDatabase().EnableFeature(gracenote::kConfigLookupDatabaseFeatureEnableMusicIDImages,
                                                        true);
    m_DbConfig[i].config.LookupDatabase().AccessMode(gracenote::kConfigOptionAccessReadWrite);
}