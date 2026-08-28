// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 21.06.2021.
//

#include "GnServiceRadioStation.hxx"

using std::string;

GnServiceRadioStation::GnServiceRadioStation()
{
    init();
}

GnServiceRadioStation::GnServiceRadioStation(uint32_t frequency, const string& band, const string& country)
{
    init();
    setFrequency(frequency);
    setBand(band);
    setCountry(country);
}

void GnServiceRadioStation::init()
{
    m_name.clear();
    m_band.clear();
    m_country.clear();
    m_gracenoteId.clear();
    m_frequency = 0;
}

GnServiceRadioStation::~GnServiceRadioStation()
= default;

void GnServiceRadioStation::setName(const std::string& name)
{
    m_name = name;
}
const std::string& GnServiceRadioStation::getName() const
{
    return m_name;
}

void GnServiceRadioStation::setBand(const std::string& band)
{
    m_band = band;
}
const std::string& GnServiceRadioStation::getBand() const
{
    return m_band;
}

void GnServiceRadioStation::setCountry(const std::string& country)
{
    m_country = country;
}
const std::string& GnServiceRadioStation::getCountry() const
{
    return m_country;
}

void GnServiceRadioStation::setGracenoteId(const std::string& id)
{
    m_gracenoteId = id;
}
const std::string& GnServiceRadioStation::getGracenoteId() const
{
    return m_gracenoteId;
}

void GnServiceRadioStation::setFrequency(uint32_t frequency)
{
    m_frequency = frequency;
}
uint32_t GnServiceRadioStation::getFrequency() const
{
    return m_frequency;
}
