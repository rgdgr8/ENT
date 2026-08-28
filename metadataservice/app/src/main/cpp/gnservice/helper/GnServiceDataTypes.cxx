// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 15.06.2020.
//
#include "GnServiceDataTypes.hxx"

using gnservice::GnServiceTypes;

GnServiceTypes::GnExternalId::GnExternalId()
{
    init();
}

GnServiceTypes::GnExternalId::~GnExternalId()
{
}

void GnServiceTypes::GnExternalId::init()
{
    m_sourceIdentifier.clear();
    m_externalId.clear();
}

GnServiceTypes::GnServiceTrackInfo::GnServiceTrackInfo()
{
    init();
}

GnServiceTypes::GnServiceTrackInfo::~GnServiceTrackInfo()
{
}

void GnServiceTypes::GnServiceTrackInfo::init()
{
    m_trackName.clear();
    m_albumName.clear();
    m_artistName.clear();
    m_albumGenre.clear();
    m_albumYear.clear();
    m_trackLength = 0;
    m_playPosition = 0;
    m_extIdList.clear();
}
