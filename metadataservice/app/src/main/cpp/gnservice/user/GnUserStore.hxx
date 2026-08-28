// Copyright 2019 <e.solutions GmbH>
// Created by miir1367 on 01.12.19.
//

#ifndef GNSERVICE_USER_GNUSERSTORE_HXX_
#define GNSERVICE_USER_GNUSERSTORE_HXX_

#include <string>

#include <gnsdk.hpp>

/*-----------------------------------------------------------------------------
 *  class GnUserStore
 *
 *  Example implementation of interface: IGnUserStore
 *  Loads and stores User data for the GnUser object. This sample stores the
 *  user data to a local file named 'user.txt'.
 *  Your application should store the user data to an appropriate location.
 */
class GnUserStore : public gracenote::IGnUserStore
{
public:
    explicit GnUserStore(const std::string& path);
    /*-----------------------------------------------------------------------------
     *  LoadSerializedUser
     */
    gracenote::GnString LoadSerializedUser(gnsdk_cstr_t clientId);

    /*-----------------------------------------------------------------------------
     *  StoreSerializedUser
     */
    bool StoreSerializedUser(gnsdk_cstr_t clientId, gnsdk_cstr_t userData);

private:
    std::string m_userFilePath;
};


#endif //GNSERVICE_USER_GNUSERSTORE_HXX_
