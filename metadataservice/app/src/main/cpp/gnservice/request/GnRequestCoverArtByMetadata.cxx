// Copyright 2020 <e.solutions GmbH>
// Created by miir1367 on 31.05.20.
//

#include <algorithm>

#include "../helper/GnServiceLogging.hxx"

#include "../event/GnServiceStatusEventHandler.hxx"
#include "../resultdb/GnLocalLookupDb.hxx"
#include "GnRequestCoverArtByMetadata.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Request
DECLARE_CHANNEL_EXTERN();
#define LOG_TAG "GnRequestCoverArtByMeta"

using std::string;
using std::vector;
using gnservice::GnLocalLookupDB;
using gracenote::GnUser;
using gracenote::GnError;
using gracenote::GnImageSize;
using gracenote::GnLookupMode;
using gracenote::GnAssetFetch;
using gracenote::musicid::GnMusicId;
using gracenote::metadata::GnDataObject;
using gracenote::metadata::GnAlbum;
using gracenote::metadata::GnContributor;
using gracenote::metadata::GnAsset;
using gracenote::metadata::GnResponseDataMatches;
using gracenote::metadata::GnDataMatch;
using gracenote::metadata::GnResponseAlbums;
using gracenote::metadata::GnArtist;
using gracenote::metadata::kDataLevel_1;
using gracenote::metadata::kDataLevel_2;
using gracenote::metadata::kDataLevel_3;
using gracenote::metadata::kDataLevel_4;
using gracenote::kLookupModeLocal;
using gracenote::kLookupModeOnlineNoCache;
using gracenote::kLookupDataContent;

GnRequestCoverArtByMetadata::GnRequestCoverArtByMetadata(IGnCoverArtEventListener* listener,
                                                         GnUser* user,
                                                         GnLocalLookupDB* resultDb,
                                                         GnServiceAlbumInfo& albumInfo) :
    GnRequest(user, resultDb),
    m_listener(listener),
    m_albumInfo(albumInfo),
    m_imageData()
{
    setRequestName("REQUEST_COVERART");
}

void GnRequestCoverArtByMetadata::executeTask()
{
    USE_CHANNEL();
    ALOG_INFO_CV("title='%s', artist='%s', album='%s'",
                   m_albumInfo.Track.c_str(),
                   m_albumInfo.Artist.c_str(),
                   m_albumInfo.Name.c_str());

    GnDataObject  match;
    GnAlbum       album   = nullptr;
    GnContributor contrib = nullptr;
    GnAsset       asset   = nullptr;

    if ( m_gnUser == nullptr )
    {
        // GnService was not initialized, no user available
        // we cannot continue
        ALOG_WARN("gracenote user is not initialized");
        goto GOTO_END;
    }

    if (m_albumInfo.Artist.empty() && m_albumInfo.Name.empty() )
    {
        // we need at least either an artist or an album to continue
        // the title is optional
        ALOG_WARN("no artist or album name given");
        goto GOTO_END;
    }

    try
    {
        match = getMatchFromMetadata(m_albumInfo.Track,
                                     m_albumInfo.Artist,
                                     m_albumInfo.Name);
    }
    catch (GnError& e)
    {
        ALOG_ERR_CV("could not find match. %s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription() );
        goto GOTO_END;
    }

    if (!match.IsNull())
    {
        if ( match.IsType(GnAlbum::GnType()) )
        {
            album = GnAlbum::From(match);
            ALOG_INFO("match ist of type album, trying to get album cover");
            getAlbumCover(album);
        }
        else if ( match.IsType(GnContributor::GnType()) )
        {
            // retrieve artist image here
            ALOG_INFO("match is of type contributor, artist image");
        }
    }
    else
    {
        ALOG_INFO("no match received");
    }

GOTO_END:
    sendResultEvent(m_albumInfo, m_imageData);
    finished();
}

GnDataObject GnRequestCoverArtByMetadata::getMatchFromMetadata(const string& album, const string& artist, const string& title)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("ENTER, title='%s' artist='%s' album='%s'",
                  album.c_str(), artist.c_str(), title.c_str());
    GnDataObject finalMatch = GnDataObject();

    ALOG_INFO("*****MusicID Query*****\n");

    auto* pStatusEventHandler = new GnServiceStatusEventHandler(const_cast<gnsdk_str_t>("MusicID Query"));

    GnMusicId musicId(*m_gnUser, pStatusEventHandler);
    musicId.Options().ResultSingle(true);
    musicId.Options().LookupData(kLookupDataContent, true);
    musicId.Options().PreferResultCoverart(true);

    // Let's try a local lookup first
    musicId.Options().LookupMode(kLookupModeLocal);


    // Set the input text as album title, artist name, track title and perform the query
    GnResponseDataMatches response = musicId.FindMatches(title.c_str(),	// Album Title
                                                          album.c_str(),	// Track Title
                                                          artist.c_str(),	// Artist Name
                                                          GNSDK_NULL,
                                                          GNSDK_NULL);
    // See how many matches were found
    gnsdk_uint32_t count = response.DataMatches().count();
    GnDataMatch match = *(response.DataMatches().begin());

    if (count == 0 || !match.IsAlbum() )
    {
        ALOG_INFO("no results found locally, trying online query\n");
        /* Change the lookup mode on the query handle to 'Online No Cache.'
         * Query caching is not used for automotive as it expires entries too quickly.
         * Automotive use cases instead use the result DB to persist results in an
         * embedded database.
         */
        musicId.Options().LookupMode(kLookupModeOnlineNoCache);

        response = musicId.FindMatches(title.c_str(),  // Album Title
                                       album.c_str(),	// Track Title
                                       artist.c_str(),	// Artist Name
                                       GNSDK_NULL,
                                       GNSDK_NULL);
        // Check if we have a match
        count = response.DataMatches().count();
    } else {
        ALOG_INFO("result found locally");
    }

    if (count != 0)
    {
        match = *(response.DataMatches().begin());

        if (match.IsAlbum())
        {
            GnAlbum foundAlbum = match.GetAsAlbum();
            bool fullResult = foundAlbum.IsFullResult();

            /* if we only have a partial result, we do a follow-up query to retrieve the full
             * album */
            if (!fullResult)
            {
                /* do followup query to get full object. Setting the partial album as the query
                 * input. */
                GnResponseAlbums followupResponse = musicId.FindAlbums(foundAlbum);

                /* now our first album is the desired result with full data */
                foundAlbum = *(followupResponse.Albums().begin());
            }

            ALOG_INFO_CV("Final match Album title='%s', artist='%s', genre='%s', year='%s'",
                         foundAlbum.Title().Display(),
                         foundAlbum.Artist().Name().Display(),
                         foundAlbum.Genre(kDataLevel_2),
                         foundAlbum.Year());
            GnArtist foundArtist = foundAlbum.Artist();
            if ( !foundArtist.IsNull() )
            {
                GnContributor contrib = foundArtist.Contributor();
                if ( !foundArtist.Contributor().IsNull() )
                {
                    ALOG_DEBUG_CV("artist origin 1='%s', origin 2='%s', origin 3='%s', origin 4='%s'",
                                  foundArtist.Contributor().Origin(kDataLevel_1),
                                  foundArtist.Contributor().Origin(kDataLevel_2),
                                  foundArtist.Contributor().Origin(kDataLevel_3),
                                  foundArtist.Contributor().Origin(kDataLevel_4));
                    ALOG_DEBUG_CV("artist era 1='%s', era 2='%s', era 3='%s'",
                                  foundArtist.Contributor().Era(kDataLevel_1),
                                  foundArtist.Contributor().Era(kDataLevel_2),
                                  foundArtist.Contributor().Era(kDataLevel_3));
                    ALOG_DEBUG_CV("artist type 1='%s', type 2='%s'",
                                  foundArtist.Contributor().Era(kDataLevel_1),
                                  foundArtist.Contributor().Era(kDataLevel_1));
                    ALOG_DEBUG_CV("artist birth date='%s', birth place='%s'",
                                  foundArtist.Contributor().BirthDate(),
                                  foundArtist.Contributor().BirthPlace());
                }
            }

            finalMatch = foundAlbum;

            m_albumInfo.Name = foundAlbum.Title().Display();
            m_albumInfo.Artist = foundAlbum.Artist().Name().Display();
            m_albumInfo.Genre = foundAlbum.Genre(kDataLevel_2);
            m_albumInfo.Year = foundAlbum.Year();
        }
        else
        {
            GnContributor contrib = match.GetAsContributor();

            /* contributor match is always a full result */
            ALOG_INFO_CV("Final match Contributor Name: '%s'",
                         contrib.NamesOfficial().at(0)->Display());
            finalMatch = contrib;
        }
    }
    else
    {
        ALOG_WARN("no match found for the input");
    }

    ALOG_DEBUG("EXIT");
    return finalMatch;
}

void GnRequestCoverArtByMetadata::getAlbumCover(const GnAlbum& album)
{
    GnAsset asset = nullptr;
    GnImageSize requestedImageSize = gracenote::kImageSize300;

    if ( !album.IsNull() )
    {
        asset = album.CoverArt().Asset(requestedImageSize);
    }

    if ( !asset.IsNull() )
    {
        getImageFromAsset(asset);
    }
    else
    {
        USE_CHANNEL();
        ALOG_WARN("no asset found for album");
    }
}

void GnRequestCoverArtByMetadata::getImageFromAsset(const GnAsset& asset)
{
    USE_CHANNEL();
    gnsdk_cstr_t url = asset.UrlGnsdk();

    if ( (GNSDK_NULL != url) )
    {
        if ( strcmp("", url) != 0 )
        {
            bool imageFound = getImageFromAssetUrlLocal(url, m_imageData);
            if (!imageFound)
            {
                ALOG_INFO("no image found locally, trying online");
                if ((getImageFromAssetUrlOnline(url, m_imageData))) {
                    ALOG_DEBUG("image found online");
                }
                else
                {
                    ALOG_WARN("no image found online");
                }
            }
        }
        else
        {
            ALOG_WARN("asset url is empty");
        }
    }
    else
    {
        ALOG_WARN("invalid asset url");
    }
}

bool GnRequestCoverArtByMetadata::getImageFromAssetUrlLocal(const gnsdk_cstr_t& url,
                                                            vector<unsigned char>& imgData)
{
    return getImageFromAssetUrl(kLookupModeLocal, url, imgData);
}

bool GnRequestCoverArtByMetadata::getImageFromAssetUrlOnline(const gnsdk_cstr_t& url,
                                                             vector<unsigned char>& imgData)
{
    return getImageFromAssetUrl(kLookupModeOnlineNoCache, url, imgData);
}

bool GnRequestCoverArtByMetadata::getImageFromAssetUrl(const GnLookupMode lookupMode,
                                                       const gnsdk_cstr_t& url,
                                                       vector<unsigned char>& imgData)
{
    USE_CHANNEL();
    bool ret = false;
    bool online = lookupMode == kLookupModeOnlineNoCache;
    gnsdk_cstr_t eventHandlerString = "Local Image Fetch";
    if ( online )
    {
        eventHandlerString = "Online Image Fetch";
    }

    ALOG_INFO_CV("attempting %s image fetch", online ? "online" : "local");
    try
    {
        auto p_StatusEventHandler = new GnServiceStatusEventHandler(const_cast<gnsdk_str_t>(eventHandlerString));
        GnAssetFetch assetFetch(*m_gnUser, url, lookupMode, p_StatusEventHandler);

        if ( (GNSDK_NULL != assetFetch.Data()) && ( 0 != assetFetch.Size() ))
        {
            ALOG_INFO_CV("image found %s, type='%s', siize=%li", online ? "online" : "locally",
                         assetFetch.Type(), assetFetch.Size());
            imgData.resize(assetFetch.Size());
            std::copy(assetFetch.Data(), assetFetch.Data()+assetFetch.Size(), imgData.begin());
            ret = true;
        }
    }
    catch (GnError& e)
    {
        ALOG_ERR_CV("asset fetch failed, %s 0x%x : %s", e.ErrorAPI(), e.ErrorCode(), e.ErrorDescription());
    }

    return ret;
}

void GnRequestCoverArtByMetadata::saveImageToResultDb(const GnAsset& asset,
                                                      const vector<unsigned char>& imgData)
{
    if ( m_resultDb != nullptr )
    {
        USE_CHANNEL();
        m_resultDb->addImage(imgData.data(), imgData.size(), asset);
        ALOG_INFO("image written to result db");
    }
}

void GnRequestCoverArtByMetadata::sendResultEvent(const GnServiceAlbumInfo& albumInfo,
                                                  const vector<unsigned char>& imgData)
{
    USE_CHANNEL();
    ALOG_DEBUG("ENTER");
    if ( m_listener == nullptr )
    {
        ALOG_WARN("no listener found, not sending event");
        return;
    }

    m_listener->onCoverArtEvent_dataReceived(albumInfo, imgData, m_jobId);
}