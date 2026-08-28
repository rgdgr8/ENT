// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 31.05.20.
//

#ifndef GNSERVICE_REQUEST_GNREQUESTCOVERARTBYMETADATA_HXX_
#define GNSERVICE_REQUEST_GNREQUESTCOVERARTBYMETADATA_HXX_

#include <string>
#include <vector>

#include "GnRequest.hxx"
#include "../helper/GnServiceDataTypes.hxx"
#include "../event/IGnCoverArtEventListener.hxx"
#include "../resultdb/GnLocalLookupDb.hxx"

class GnRequestCoverArtByMetadata : public GnRequest
{
public:
    GnRequestCoverArtByMetadata(IGnCoverArtEventListener* listener,
                                gracenote::GnUser* user,
                                gnservice::GnLocalLookupDB* resultDb,
                                GnServiceAlbumInfo& albumInfo);

private:

    gracenote::metadata::GnDataObject getMatchFromMetadata(const std::string& album,
                                                           const std::string& artist,
                                                           const std::string& title);

    void getAlbumCover(const gracenote::metadata::GnAlbum& album);
    void getImageFromAsset(const gracenote::metadata::GnAsset& asset);
    bool getImageFromAssetUrlLocal(const gnsdk_cstr_t& url, std::vector<unsigned char>& imgData);
    bool getImageFromAssetUrlOnline(const gnsdk_cstr_t& url, std::vector<unsigned char>& imgData);
    bool getImageFromAssetUrl(const gracenote::GnLookupMode mode,
                              const gnsdk_cstr_t& url,
                              std::vector<unsigned char>& imgData);
    void saveImageToResultDb(const gracenote::metadata::GnAsset& asset, const std::vector<unsigned char>& imgData);

    // sending events to listener
    void sendResultEvent(const GnServiceAlbumInfo& albumInfo,
                         const std::vector<unsigned char>& imgData);

    // inherited from abstract class GnRequest
    virtual void executeTask();

private:
    IGnCoverArtEventListener* m_listener;
    GnServiceAlbumInfo m_albumInfo;
    std::vector<unsigned char> m_imageData;

};


#endif //GNSERVICE_REQUEST_GNREQUESTCOVERARTBYMETADATA_HXX_
