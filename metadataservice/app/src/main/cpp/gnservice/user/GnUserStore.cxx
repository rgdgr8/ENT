// Copyright 2019 <e.solutions GmbH>
// Created by mike on 01.12.19.
//

#include <dirent.h>
#include <fstream>

#include "GnUserStore.hxx"
#include "../helper/GnServiceLogging.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Gnuser
DECLARE_CHANNEL();
#define LOG_TAG "GnUserStore"

using std::string;
using std::fstream;
using std::ios_base;
using gracenote::GnString;

/*-----------------------------------------------------------------------------
 *  class GnUserStore
 *
 *  Example implementation of interface: IGnUserStore
 *  Loads and stores User data for the GnUser object. This sample stores the
 *  user data to a local file named 'user.txt'.
 *  Your application should store the user data to an appropriate location.
 */

GnUserStore::GnUserStore(const string& path) :
    m_userFilePath(path)
{
}

GnString GnUserStore::LoadSerializedUser(gnsdk_cstr_t clientId)
{
    USE_CHANNEL();
    fstream  userRegFile;
    string   fileName;
    string   serialized;
    GnString userData;

    fileName  = m_userFilePath;
    fileName += "/user_";
    fileName += clientId;
    fileName += ".txt";

    ALOG_INFO_CV("trying user file '%s'\n", fileName.c_str());


    userRegFile.open(fileName.c_str());
    if ( !userRegFile.is_open() )
    {
        ALOG_ERR_CV("could not open user file '%s'\n", fileName.c_str());
    }
    else
    {
        ALOG_INFO_CV("opened user file '%s'\n", fileName.c_str());
        userRegFile >> serialized;
        userData = serialized.c_str();
        ALOG_INFO_CV("serialized user: '%s'\n", serialized.c_str());
    }
    return userData;
}

/*-----------------------------------------------------------------------------
    *  StoreSerializedUser
    */
bool GnUserStore::StoreSerializedUser(gnsdk_cstr_t clientId, gnsdk_cstr_t userData)
{
    fstream userRegFile;
    string  fileName;

    fileName  = m_userFilePath;
    fileName += "/user_";
    fileName += clientId;
    fileName += ".txt";

    /* store user data to file */
    userRegFile.open(fileName.c_str(), ios_base::out);
    if (!userRegFile.fail())
    {
        userRegFile << userData;
        return true;
    }
    return false;
}