// Copyright 2021 <e.solutions GmbH>
// Created by miir1367 on 21.06.2021.
//

#ifndef GNSERVICE_REQUEST_GNREQUESTRADIOSTATIONDATA_HXX_
#define GNSERVICE_REQUEST_GNREQUESTRADIOSTATIONDATA_HXX_

#include <string>

#include "GnRequest.hxx"
#include "../radioresultdb/GnRadioResultDb.hxx"
#include "../helper/GnServiceDataTypes.hxx"
#include "../helper/GnServiceRadioStation.hxx"
#include "../event/IGnRadioStationEventListener.hxx"

class GnRequestRadioStationData : public GnRequest
{
public:
    GnRequestRadioStationData(IGnRadioStationEventListener* listener,
                              gracenote::GnUser* user,
                              GnRadioResultDb* resultDb,
                              GnServiceRadioStation& radioStation,
                              GnServicePositionData);

    virtual void executeTask();
    virtual ~GnRequestRadioStationData() noexcept;

private:

    uint32_t findStationsLocal();
    void fetchAndStoreStationLogo(gracenote::metadata::GnRadioStation&  station);
    void checkAndUpdateStations();
    uint32_t displayResults(gracenote::metadata::GnResponseRadioStations& response);
    void setupRadioId();
    void setupRadioIdOptions();
    void setFrequency(const uint32_t frequency);
    void setBand(const std::string& band);
    void setCountry(const std::string& country);
    void setPosition();

private:
    IGnRadioStationEventListener* m_listener;
    GnServiceRadioStation m_radioStation;
    GnServicePositionData m_positionData;
    GnRadioResultDb* m_radioDb;
    gracenote::radioid::GnRadioId m_radioId;
};


#endif //GNSERVICE_REQUEST_GNREQUESTRADIOSTATIONDATA_HXX_
