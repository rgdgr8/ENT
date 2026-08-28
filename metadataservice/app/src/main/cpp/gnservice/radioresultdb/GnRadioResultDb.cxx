//
// Created by mike on 21.10.21.
// Copyright (2021) <michael@irsiegler.de>
//
#include <sys/stat.h>
#include <string>
//#include <memory>
#include <mutex>

#include <gnsdk.hpp>

#include "../helper/GnServiceLogging.hxx"

#ifdef GN_INTEGRATE_FRAMEWORK
#include <osal/FileSys.hxx>
#endif

#include "GnRadioResultDb.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ ResultDb_Radio
DECLARE_CHANNEL();
#define LOG_TAG "GnRadioResultDb"

using std::string;
using std::recursive_mutex;
using std::make_unique;
using std::lock_guard;

using gracenote::lookupdatabase::GnLookupDatabase;
using gracenote::GnError;
using gracenote::GnString;
using gracenote::metadata::GnDataObject;
using gracenote::metadata::GnRenderOptions;

static recursive_mutex radioDbMutex;

GnRadioResultDb::GnRadioResultDb() :
        m_location(),
        m_pDbMeta(),
        m_pDbImg(),
        m_pActiveDbMeta(),
        m_pActiveDbImg(),
        m_activeIndexMeta(0),
        m_activeIndexImg(0),
        m_DbConfig(),
        m_initialized(false)
{

}

uint32_t GnRadioResultDb::init(const string& location)
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    lock_guard<recursive_mutex> lock(radioDbMutex);
    uint32_t res;

    if ( m_initialized )
    {
        ALOG_DEBUG("already initialized nothing to do");
        return 0;
    }

    m_location = location;

    res = createFolders();
    if ( res != 0 )
    {
        return res;
    }

    res = createDb();
    if ( res == 0)
    {
        m_initialized = true;
    }

    ALOG_DEBUG("EXIT");
    return res;
}

uint32_t GnRadioResultDb::createFolders()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");

    string deletePath = m_location + "/radioresultDb";
    remove(deletePath.c_str());

    string basePath = m_location + "/" + kRadioResultDbMainLocation;
    if ( mkdir(basePath.c_str(), kDbFolderAccessMode) != 0 )
    {
        if(errno != EEXIST)
        {
            ALOG_ERR_CV("could not create directory for result db, errno=%i", errno);
            return 1;
        }
    }

    for ( uint32_t index = 0; index < kMaxNumberOfResultDb; index++)
    {
        string path = basePath + "/" + radioResultDbConfig[index].subPath;
        if ( mkdir(path.c_str(), kDbFolderAccessMode) != 0 )
        {
            if ( errno != EEXIST )
            {
                ALOG_ERR_CV("could not create directory '%s' for result db, errno=%i",
                            path.c_str(), errno);
                return 1;
            }
        }

        string metaPath = path + "/" + radioResultDbConfig[index].subPathMeta;
        if ( mkdir(metaPath.c_str(), kDbFolderAccessMode) != 0 )
        {
            if ( errno != EEXIST )
            {
                ALOG_ERR_CV("could not create directory '%s' for result db, errno=%i",
                            metaPath.c_str(), errno);
                return 1;
            }
        }

        ALOG_INFO_CV("created path for radio result db %s %s: '%s'",
                     radioResultDbConfig[index].identifierMeta.c_str(),
                     radioResultDbConfig[index].identifierImg.c_str(),
                     metaPath.c_str());
        string imgPath = path + "/" + radioResultDbConfig[index].subPathImg;
        if ( mkdir(imgPath.c_str(), kDbFolderAccessMode) != 0)
        {
            if ( errno != EEXIST )
            {
                ALOG_ERR_CV("could not create directory '%s' for result db, errno=%i",
                            imgPath.c_str(), errno);
                return 1;
            }
        }

        ALOG_INFO_CV("created path for radio result db %s %s: '%s'",
                     radioResultDbConfig[index].identifierMeta.c_str(),
                     radioResultDbConfig[index].identifierImg.c_str(),
                     imgPath.c_str());


        m_DbConfig[index].pathMeta = metaPath;
        m_DbConfig[index].pathImg = imgPath;
        m_DbConfig[index].idImg = radioResultDbConfig[index].identifierImg;
        m_DbConfig[index].idMeta = radioResultDbConfig[index].identifierMeta;
    }

    ALOG_DEBUG("EXIT");
    return 0;
}

uint32_t GnRadioResultDb::createDb()
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");

    try
    {
        for (uint32_t i = 0; i < kMaxNumberOfResultDb; i++)
        {
            // database for radio station metadata
            ALOG_DEBUG_CV("trying to create result db %s", m_DbConfig[i].idMeta.c_str());
            setDbConfigMeta(i);
            m_pDbMeta[i] = new GnLookupDatabase(m_DbConfig[i].idMeta.c_str(),
                                                m_DbConfig[i].configMeta);
            // database for images
            ALOG_DEBUG_CV("trying to create result db %s", m_DbConfig[i].idImg.c_str());
            setDbConfigImg(i);
            m_pDbImg[i] = new GnLookupDatabase(m_DbConfig[i].idImg.c_str(),
                                               m_DbConfig[i].configImg);

            // set permissions for metadata file
            string filePath = m_DbConfig[i].pathMeta + "/" + kRadioResultDbMetaFile;
            setFilePermissions(filePath);

            // set permissions for logo/image file
            filePath = m_DbConfig[i].pathImg + "/" + kRadioResultDbImgFile;
            setFilePermissions(filePath);
        }
    }
    catch ( GnError& e)
    {
        ALOG_ERR_CV("could not create radio result db: error: %s, 0x%x %s",
                    e.ErrorAPI(),
                    e.ErrorCode(),
                    e.ErrorDescription());
        if ( retryCreateDb() )
        {
            return 1;
        }
    }

    m_activeIndexMeta = (m_pDbMeta[0]->Size() < kRadioDbMaxSize/2) ? 0 : 1;
    m_activeIndexImg = (m_pDbImg[0]->Size() < kRadioImgDbMaxSize/2) ? 0 : 1;

    m_pActiveDbMeta = m_pDbMeta[m_activeIndexMeta];
    m_pActiveDbImg = m_pDbImg[m_activeIndexImg];

    ALOG_INFO_CV("activating radio db for data '%s'", m_activeIndexMeta == 0 ? "a" : "b");
    ALOG_INFO_CV("activating radio db for images '%s'", m_activeIndexImg == 0 ? "a" : "b");

    ALOG_DEBUG("EXIT");
    return 0;
}

void GnRadioResultDb::displayMetaDbInfo()
{
    USE_CHANNEL();
    GnDataObject dataInfo = m_pActiveDbMeta->Info();
    GnRenderOptions render;
    GnString dataInfoString = dataInfo.Render(render.Xml());
    ALOG_INFO_CV("Radio Result DB Data: %s", dataInfoString.c_str());
    ALOG_INFO_CV("Size: %llu (%llu kB), max. Size %u (%u kB)",
                 m_pActiveDbMeta->Size(),
                 m_pActiveDbMeta->Size()/1024,
                 kRadioDbMaxSize/2,
                 (kRadioDbMaxSize/2)/1024);
}

void GnRadioResultDb::displayImgDbInfo()
{
    USE_CHANNEL();
    GnDataObject imgInfo  = m_pActiveDbImg->Info();
    GnRenderOptions render;
    GnString imgInfoString = imgInfo.Render(render.Xml());
    ALOG_INFO_CV("RadioResult DB Images: %s", imgInfoString.c_str());
    ALOG_INFO_CV("Size: %llu (%llu kB), max. Size %u (%u kB)",
                 m_pActiveDbImg->Size(),
                 m_pActiveDbImg->Size()/1024,
                 kRadioImgDbMaxSize/2,
                 (kRadioImgDbMaxSize/2)/1024);
}

void GnRadioResultDb::addImage(const gnsdk_byte_t *imgData,
                               const gnsdk_size_t size,
                               const gracenote::metadata::GnAsset &asset)
{
    USE_CHANNEL();
    ALOG_INFO_CV("writing image to result db %s", m_activeIndexImg == 0 ? "a" : "b");
    try
    {
        m_pActiveDbImg->AddImage(imgData, size, asset);
        checkImgDbSize();
    }
    catch (GnError& e)
    {
        ALOG_ERR_CV("could not store image in result db: %s, error: 0x%x %s",
                    e.ErrorAPI(),
                    e.ErrorCode(),
                    e.ErrorDescription());
    }
}

void GnRadioResultDb::checkImgDbSize()
{
    displayImgDbSize();

    if ( m_pActiveDbImg->Size() >= kRadioImgDbMaxSize/2 )
    {
        USE_CHANNEL();
        const uint32_t oldIndex = m_activeIndexImg;
        const uint32_t newIndex = m_activeIndexImg == 0 ? 1 : 0;

        ALOG_INFO_CV("%s has reached maximum size, switching to next result db %s (new index=%u)",
                     m_DbConfig[oldIndex].idImg.c_str(),
                     m_DbConfig[newIndex].idImg.c_str(),
                     newIndex);
        reCreateImgDb(newIndex);
    }
}

void GnRadioResultDb::checkMetaDbSize()
{
    displayMetaDbSize();

    if ( m_pActiveDbMeta->Size() >= kRadioDbMaxSize/2 )
    {
        USE_CHANNEL();
        const uint32_t oldIndex = m_activeIndexMeta;
        const uint32_t newIndex = m_activeIndexMeta == 0 ? 1 : 0;

        ALOG_INFO_CV("%s has reached maximum size, switching to next result db %s (new index=%u)",
                     m_DbConfig[oldIndex].idMeta.c_str(),
                     m_DbConfig[newIndex].idMeta.c_str(),
                     newIndex);
        reCreateMetaDb(newIndex);
    }
}

void GnRadioResultDb::displayImgDbSize()
{
    USE_CHANNEL();
    ALOG_INFO_CV("%s size: %llu (%llu kB), max. Size %u (%u kB)",
                 m_DbConfig[m_activeIndexImg].idImg.c_str(),
                 m_pActiveDbImg->Size(),
                 m_pActiveDbImg->Size()/1024,
                 kRadioImgDbMaxSize/2,
                 (kRadioImgDbMaxSize/2)/1024);
}

void GnRadioResultDb::displayMetaDbSize()
{
    USE_CHANNEL();
    ALOG_INFO_CV("%s size: %llu (%llu kB), max. Size %u (%u kB)",
                 m_DbConfig[m_activeIndexMeta].idMeta.c_str(),
                 m_pActiveDbMeta->Size(),
                 m_pActiveDbMeta->Size()/1024,
                 kRadioDbMaxSize/2,
                 (kRadioDbMaxSize/2)/1024);
}

void GnRadioResultDb::reCreateImgDb(const uint32_t index)
{
    lock_guard<recursive_mutex> lock(radioDbMutex);

    try
    {
        m_pDbImg[index]->Close();
        gracenote::lookupdatabase::GnLookupDatabase::Delete(m_DbConfig[index].configImg);
        m_pDbImg[index] = new GnLookupDatabase(m_DbConfig[index].idImg.c_str(),
                                               m_DbConfig[index].configImg);

        m_pActiveDbImg = m_pDbImg[index];
        m_activeIndexImg = index;
    }
    catch (GnError& e)
    {
        USE_CHANNEL();
        ALOG_ERR_CV("Error recreating image result db: %s, error: 0x%x %s",
                    e.ErrorAPI(),
                    e.ErrorCode(),
                    e.ErrorDescription());
    }

    displayImgDbInfo();
}

void GnRadioResultDb::reCreateMetaDb(const uint32_t index)
{
    try
    {
        m_pDbMeta[index]->Close();
        gracenote::lookupdatabase::GnLookupDatabase::Delete(m_DbConfig[index].configMeta);
        m_pDbMeta[index] = new GnLookupDatabase(m_DbConfig[index].idMeta.c_str(),
                                               m_DbConfig[index].configMeta);
        m_pActiveDbMeta = m_pDbMeta[index];
        m_activeIndexMeta = index;
    }
    catch (GnError& e)
    {
        USE_CHANNEL();
        ALOG_ERR_CV("Error recreating result db: %s, error: 0x%x %s",
                    e.ErrorAPI(),
                    e.ErrorCode(),
                    e.ErrorDescription());
    }

    displayMetaDbInfo();
}

void GnRadioResultDb::checkAndUpdateStations(gracenote::radioid::GnRadioId& radioId)
{
    USE_CHANNEL();
    lock_guard<recursive_mutex> lock(radioDbMutex);
    string id = m_DbConfig[m_activeIndexMeta].idMeta;

    try
    {
        radioId.Options().StationsUpdateDatabaseIdentifier(id.c_str());
        if (radioId.StationsUpdateCheck())
        {
            ALOG_INFO_CV("meta data update for the result db is necessary: %s", id.c_str());
            radioId.StationsUpdate();
            checkMetaDbSize();
        }
    }
    catch (GnError& e)
    {
        ALOG_ERR_CV("update failed: %s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
        return;
    }

}

void GnRadioResultDb::setFilePermissions(const std::string& path)
{
    USE_CHANNEL();
#ifdef GN_INTEGRATE_FRAMEWORK
    if (osal::FileSys::changeAccess(path,
                                    osal::FileAccess::owner_read |
                                    osal::FileAccess::owner_write |
                                    osal::FileAccess::group_read |
                                    osal::FileAccess::group_write))
    {
        ALOG_ERR_CV("could not set permissions for %s", path.c_str());
    }
#else
    if ( chmod(path.c_str(), 0660) )
    {
        ALOG_ERR_CV("could not set permissions for %s", path.c_str());
    }
#endif
}

uint32_t GnRadioResultDb::retryCreateDb()
{
    USE_CHANNEL();

    if (deleteAllDbFiles())
    {
        return 1;
    }

    for ( uint32_t i = 0; i < kMaxNumberOfResultDb; i++ )
    {
        try {
            // database for radio station metadata
            ALOG_DEBUG_CV("trying to create result db %s", m_DbConfig[i].idMeta.c_str());
            setDbConfigMeta(i);
            m_pDbMeta[i] = new GnLookupDatabase(m_DbConfig[i].idMeta.c_str(),
                                                m_DbConfig[i].configMeta);
            // database for images
            ALOG_DEBUG_CV("trying to create result db %s", m_DbConfig[i].idImg.c_str());
            setDbConfigImg(i);
            m_pDbImg[i] = new GnLookupDatabase(m_DbConfig[i].idImg.c_str(),
                                               m_DbConfig[i].configImg);
        }
        catch (GnError& e)
        {
            ALOG_ERR_CV("retry creating radio result db failed: error: %s, 0x%x %s",
                        e.ErrorAPI(),
                        e.ErrorCode(),
                        e.ErrorDescription());
            return 1;
        }

        // set permissions for metadata file
        string filePath = m_DbConfig[i].pathMeta + "/" + kRadioResultDbMetaFile;
        setFilePermissions(filePath);

        // set permissions for logo/image file
        filePath = m_DbConfig[i].pathImg + "/" + kRadioResultDbImgFile;
        setFilePermissions(filePath);
    }
    return 0;
}

uint32_t GnRadioResultDb::deleteAllDbFiles()
{
    USE_CHANNEL();
    ALOG_WARN("deleting all radio result database files");
    uint32_t res = 0;

    for ( uint32_t index = 0; index < kMaxNumberOfResultDb; index++ )
    {
        // in case one of the result db files have been opened already, close them for gnsdk
        try
        {
            if (m_pDbMeta[index] != nullptr) {
                m_pDbMeta[index]->Close();
                m_pDbMeta[index] = nullptr;
            }

            if (m_pDbImg[index] != nullptr) {
                m_pDbImg[index]->Close();
                m_pDbImg[index] = nullptr;
            }
        }
        catch ( GnError& e)
        {
            ALOG_ERR_CV("could not close result db, error: %s, 0x%x %s",
                        e.ErrorAPI(),
                        e.ErrorCode(),
                        e.ErrorDescription());
        }

        // now delete the files
        string pathMeta = m_DbConfig[index].pathMeta + "/" + kRadioResultDbMetaFile;
        ALOG_WARN_CV("deleting file %s", pathMeta.c_str());
        if ( std::remove(pathMeta.c_str()) )
        {
            res = 1;
        }
        string pathImg = m_DbConfig[index].pathImg + "/" + kRadioResultDbImgFile;
        ALOG_WARN_CV("deleting file %s", pathImg.c_str());
        if ( std::remove(pathImg.c_str()) ) {
            res = 1;
        }
    }

    return res;
}

void GnRadioResultDb::setDbConfigMeta(const uint32_t i)
{
    m_DbConfig[i].configMeta.LookupDatabase().StorageLocation(
            gracenote::kConfigLookupDatabaseLocationAll,
            m_DbConfig[i].pathMeta.c_str());
    m_DbConfig[i].configMeta.LookupDatabase().EnableFeature(
            gracenote::kConfigLookupDatabaseFeatureEnableRadioStationId,
            true);
    m_DbConfig[i].configMeta.LookupDatabase().AccessMode(
            gracenote::kConfigOptionAccessReadWrite);
    m_DbConfig[i].configMeta.LookupDatabase().ExpirationTime(
            gracenote::kConfigLookupDatabaseExpirationTimeHours,
            kRadioDbExpirationTime);
}

void GnRadioResultDb::setDbConfigImg(const uint32_t i)
{
    m_DbConfig[i].configImg.LookupDatabase().StorageLocation(
            gracenote::kConfigLookupDatabaseLocationAll,
            m_DbConfig[i].pathImg.c_str());
    m_DbConfig[i].configImg.LookupDatabase().EnableFeature(
            gracenote::kConfigLookupDatabaseFeatureEnableRadioStationIdImages,
            true);
    m_DbConfig[i].configImg.LookupDatabase().AccessMode(
            gracenote::kConfigOptionAccessReadWrite);
}
