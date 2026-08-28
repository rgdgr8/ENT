// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 31.05.2021.
//

#include "GnServiceTrackInfo.hxx"

GnServiceTrackInfo::GnServiceTrackInfo()
{
    init();
}

GnServiceTrackInfo::~GnServiceTrackInfo()
= default;

void GnServiceTrackInfo::init()
{
    m_trackName.clear();
    m_artistName.clear();
    m_albumName.clear();
    m_albumGenre.clear();
    m_albumYear.clear();
    m_mood.clear();
    m_tempo.clear();
    m_gnId.clear();
    m_gnUId.clear();
    m_trackLength = 0;
    m_playPosition = 0;
    m_externalIdList.clear();
    m_externalAlbumIdList.clear();
}

void GnServiceTrackInfo::setTrackName(const std::string &name)
{
    m_trackName = name;
}

const std::string& GnServiceTrackInfo::getTrackName() const
{
    return m_trackName;
}

void GnServiceTrackInfo::setAlbumName(const std::string &name)
{
    m_albumName = name;
}

const std::string& GnServiceTrackInfo::getAlbumName() const
{
    return m_albumName;
}

void GnServiceTrackInfo::setArtistName(const std::string &name)
{
    m_artistName = name;
}

const std::string& GnServiceTrackInfo::getArtistName() const
{
    return m_artistName;
}

void GnServiceTrackInfo::setAlbumGenre(const std::string &name)
{
    m_albumGenre = name;
}

const std::string& GnServiceTrackInfo::getAlbumGenre() const
{
    return m_albumGenre;
}

void GnServiceTrackInfo::setAlbumYear(const std::string &year)
{
    m_albumYear = year;
}

const std::string& GnServiceTrackInfo::getAlbumYear() const
{
    return m_albumYear;
}

void GnServiceTrackInfo::setMood(const std::string &mood)
{
    m_mood = mood;
}

const std::string& GnServiceTrackInfo::getMood() const
{
    return m_mood;
}

void GnServiceTrackInfo::setTempo(const std::string &tempo)
{
    m_tempo = tempo;
}

const std::string& GnServiceTrackInfo::getTempo() const
{
    return m_tempo;
}

void GnServiceTrackInfo::setGnId(const std::string &gnId)
{
    m_gnId = gnId;
}

const std::string& GnServiceTrackInfo::getGnId() const
{
    return m_gnId;
}

void GnServiceTrackInfo::setGnUId(const std::string &gnUId)
{
    m_gnUId = gnUId;
}

const std::string& GnServiceTrackInfo::getGnUId() const
{
    return m_gnUId;
}

void GnServiceTrackInfo::setTrackLength(const int32_t length)
{
    m_trackLength = length;
}

int32_t GnServiceTrackInfo::getTrackLength() const
{
    return m_trackLength;
}

void GnServiceTrackInfo::setPlayPosition(const int32_t playPos)
{
    m_playPosition = playPos;
}

int32_t GnServiceTrackInfo::getPlayPosition() const
{
    return m_playPosition;
}

void GnServiceTrackInfo::addExternalId(const GnServiceExternalId& externalId)
{
    m_externalIdList.push_back(externalId);
}

const std::vector<GnServiceExternalId> &GnServiceTrackInfo::getExternalIdList() const
{
    return m_externalIdList;
}

void GnServiceTrackInfo::addExternalAlbumId(const GnServiceExternalId &externalId)
{
    m_externalAlbumIdList.push_back(externalId);
}

const std::vector<GnServiceExternalId>& GnServiceTrackInfo::getExternalAlbumIdList() const
{
    return m_externalAlbumIdList;
}