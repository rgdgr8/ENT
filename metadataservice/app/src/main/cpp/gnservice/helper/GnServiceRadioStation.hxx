// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 21.06.2021.
//

#ifndef GNSERVICE_HELPER_GNSERVICERADIOSTATION_HXX_
#define GNSERVICE_HELPER_GNSERVICERADIOSTATION_HXX_

#include <string>

class GnServiceRadioStation
{
public:
    GnServiceRadioStation();
    GnServiceRadioStation(uint32_t frequency, const std::string& band, const std::string& country);
    ~GnServiceRadioStation();

    void init();

    void setName(const std::string& name);
    const std::string& getName() const;
    void setBand(const std::string& band);
    const std::string& getBand() const;
    void setCountry(const std::string& country);
    const std::string& getCountry() const;
    void setGracenoteId(const std::string& id);
    const std::string& getGracenoteId() const;
    void setFrequency(uint32_t frequency);
    uint32_t getFrequency() const;

private:
    std::string m_name;
    std::string m_band;
    std::string m_country;
    std::string m_gracenoteId;
    uint32_t    m_frequency;
};


#endif //GNSERVICE_HELPER_GNSERVICERADIOSTATION_HXX_
