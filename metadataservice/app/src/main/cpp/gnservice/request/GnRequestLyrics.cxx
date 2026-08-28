// Copyright 2023 <e.solutions GmbH>
// Created by miir1367 on 23.05.2023.
//
#include <string>
#include <vector>
#include <unistd.h>

#include "../event/GnServiceStatusEventHandler.hxx"
#include "../helper/GnServiceLogging.hxx"

#include "GnRequestLyrics.hxx"

/*================================================================================*\
 * TRACING
 *================================================================================*/
#define __CHANNEL__EXT__ Request
DECLARE_CHANNEL_EXTERN();
#define LOG_TAG "GnRequestLyrics"

using std::string;
using std::vector;
using gracenote::metadata::GnContent;
using gracenote::metadata::GnTrack;
using gracenote::metadata::GnAsset;
using gracenote::GnAssetFetch;

GnRequestLyrics::GnRequestLyrics(IGnLyricsEventListener* listener,
                                 gracenote::GnUser* user,
                                 gnservice::GnLocalLookupDB* resultDb,
                                 gracenote::metadata::GnTrack& track,
                                 GnServiceLyricsType type,
                                 bool familyFriendly) :
    GnRequest(user, resultDb),
    m_listener(listener),
    m_curTrack(track),
    m_type(type),
    m_familyFriendly(familyFriendly),
    m_trackUpdated(false)
{
    setRequestName("REQUEST_LYRICS");
    if ( !m_curTrack.IsNull() )
    {
        m_trackUpdated = true;
    }
}

void GnRequestLyrics::executeTask()
{
    USE_CHANNEL();

    while ( isRunning() )
    {
        if ( m_trackUpdated )
        {
            ALOG_INFO_CV("requesting lyrics for new track: '%s'", m_curTrack.Title().Display());
            getLyrics();
            m_trackUpdated = false;
        }
        usleep(500000); // don't hog cpu
    }

    ALOG_INFO("task finished");
    m_listener->onLyricsEvent_finished(getJobId());
    finished();
}

void GnRequestLyrics::getLyrics()
{
    USE_CHANNEL();

    GnAsset asset;
    gnsdk_cstr_t lyricsUrl;
    vector<unsigned char> lyrics;
    uint32_t count;
    GnContent content;

    ALOG_DEBUG_CV("trying to get lyrics for title='%s', GNID='%s', type=%d",
                 m_curTrack.Title().Display(),
                 m_curTrack.GnId(),
                 m_type);

    if ( m_familyFriendly )
    {
        ALOG_DEBUG("setting content type to 'lyrics_clean'");
        content = m_curTrack.ChildGet<GnContent>(GNSDK_GDO_TYPE_CONTENT"!lyrics_clean");
        count = m_curTrack.ChildCount(GNSDK_GDO_TYPE_CONTENT"!lyrics_clean");
    }
    else
    {
        ALOG_DEBUG("setting content type to 'lyrics'");
        content = m_curTrack.ChildGet<GnContent>(GNSDK_GDO_TYPE_CONTENT"!lyrics");
        count = m_curTrack.ChildCount(GNSDK_GDO_TYPE_CONTENT"!lyrics");
        if ( count == 0 ) // if no lyrics is found, try clean lyrics (family friendly)
        {
            ALOG_INFO("no lyrics found, trying 'lyrics_clean'");
            content = m_curTrack.ChildGet<GnContent>(GNSDK_GDO_TYPE_CONTENT"!lyrics_clean");
            count = m_curTrack.ChildCount(GNSDK_GDO_TYPE_CONTENT"!lyrics_clean");
        }
    }

    if ( count == 0 )
    {
        ALOG_WARN("no lyrics content available, count=0");
        goto GOTO_END;
    }

    asset = getAssetForLyrics(content);
    if ( asset.IsNull() )
    {
        ALOG_WARN("lyrics asset is null, no lyrics available");
        goto GOTO_END;
    }

    lyricsUrl = asset.UrlHttps();

    if ( (lyricsUrl != GNSDK_NULL) && (strcmp("", lyricsUrl) != 0) )
    {
        auto p_StatusEventHandler = new GnServiceStatusEventHandler(
                const_cast<gnsdk_str_t>("lyrics online lookup"));
        GnAssetFetch assetFetch(*m_gnUser, lyricsUrl, gracenote::kLookupModeOnlineNoCache,
                                p_StatusEventHandler);
        gnsdk_cstr_t type = assetFetch.Type();
        const gnsdk_byte_t *json = assetFetch.Data();
        const gnsdk_size_t size = assetFetch.Size();

        if ( json && size )
        {
            ALOG_INFO_CV("successfully fetched lyrics:type=%s, size=%lu, url=%s",
                         type, size, lyricsUrl);
            lyrics.resize(assetFetch.Size());
            std::copy(assetFetch.Data(), assetFetch.Data()+assetFetch.Size(), lyrics.begin());

            string lyricsString = {lyrics.begin(), lyrics.end()};
            ALOG_DEBUG_CV("Lyrics: %s", lyricsString.c_str());
        }
    }
    else
    {
        ALOG_WARN("lyrics url is empty, no lyrics available");
    }

GOTO_END:
    sendResultEvent(lyrics);
}

void GnRequestLyrics::sendResultEvent(const std::vector<unsigned char> &lyricsData)
{
    m_listener->onLyricsEvent_dataReceived(lyricsData, getJobId());
}

GnAsset GnRequestLyrics::getAssetForLyrics(const gracenote::metadata::GnContent &content)
{
    USE_CHANNEL();
    ALOG_DEBUG_CV("getting asset, type=%d", m_type);
    switch ( m_type )
    {
        case GnServiceLyricsType::LYRICS_STATIC:
            return content.ChildGet<GnAsset>(GNSDK_GDO_TYPE_ASSET"!lyrics_timing_none");
        case GnServiceLyricsType::LYRICS_LINEBYLINE:
            return content.ChildGet<GnAsset>(GNSDK_GDO_TYPE_ASSET"!lyrics_timing_line");
        case GnServiceLyricsType::LYRICS_WORDBYWORD:
            return content.ChildGet<GnAsset>(GNSDK_GDO_TYPE_ASSET"!lyrics_timing_word");
    }
    return GNSDK_NULL;
}

void GnRequestLyrics::setCurrentTrack(const GnTrack &track)
{
    m_curTrack = track;
    m_trackUpdated = true;
}